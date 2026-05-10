"""
Crawler toàn bộ dữ liệu từ trang Wikipedia:
https://vi.wikipedia.org/wiki/Biển_báo_giao_thông_tại_Việt_Nam

Dữ liệu được lưu vào:
  - output/bien_bao_data.json        → toàn bộ nội dung dạng JSON
  - output/tables_csv/table_*.csv    → các bảng biển báo dạng CSV
  - output/images/                   → toàn bộ ảnh tải về

Yêu cầu:
    pip install requests beautifulsoup4 pandas
"""

import os
import json
import time
import re
import csv
import urllib.parse
from pathlib import Path

import requests
from bs4 import BeautifulSoup
import pandas as pd

# ── Cấu hình ─────────────────────────────────────────────────────────────────
URL = "https://vi.wikipedia.org/wiki/Bi%E1%BB%83n_b%C3%A1o_giao_th%C3%B4ng_t%E1%BA%A1i_Vi%E1%BB%87t_Nam"
OUTPUT_DIR = Path("output")
IMAGE_DIR  = OUTPUT_DIR / "images"
HEADERS    = {
    # Giả lập trình duyệt thật để tránh bị Wikimedia robot-policy chặn
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/124.0.0.0 Safari/537.36"
    ),
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Accept-Language": "vi,en-US;q=0.9,en;q=0.8",
    "Accept-Encoding": "gzip, deflate, br",
    "Connection": "keep-alive",
    "Referer": "https://vi.wikipedia.org/",
}

# ── Cấu hình delay & retry ────────────────────────────────────────────────────
DELAY_PAGE          = 2.0   # giây chờ sau khi tải trang HTML chính
DELAY_BETWEEN_IMGS  = 1.5   # giây chờ cơ bản giữa mỗi ảnh
DELAY_JITTER        = 0.5   # ±jitter ngẫu nhiên thêm vào (tránh pattern đều đặn)
RETRY_MAX           = 5     # số lần thử lại tối đa
RETRY_BACKOFF_BASE  = 3.0   # giây – lần 1: 3s, lần 2: 9s, lần 3: 27s …
RETRY_429_MIN_WAIT  = 10.0  # chờ tối thiểu khi gặp 429 (giây)
# ─────────────────────────────────────────────────────────────────────────────

import random


def _jitter(base: float) -> float:
    """Thêm nhiễu ngẫu nhiên ±DELAY_JITTER vào base delay."""
    return max(0.1, base + random.uniform(-DELAY_JITTER, DELAY_JITTER))


def safe_request(url: str, timeout: int = 30) -> requests.Response:
    """
    GET với retry + exponential backoff + jitter.
    Xử lý đặc biệt 429 Too Many Requests: đọc Retry-After header
    hoặc chờ ít nhất RETRY_429_MIN_WAIT giây.
    """
    for attempt in range(1, RETRY_MAX + 1):
        try:
            resp = requests.get(url, headers=HEADERS, timeout=timeout)

            if resp.status_code == 429:
                retry_after_hdr = resp.headers.get("Retry-After", "")
                if retry_after_hdr.isdigit():
                    wait = max(int(retry_after_hdr), RETRY_429_MIN_WAIT)
                else:
                    wait = max(RETRY_BACKOFF_BASE ** attempt, RETRY_429_MIN_WAIT)
                wait = _jitter(wait)
                print(f"  [!] 429 – chờ {wait:.1f}s (lần {attempt}/{RETRY_MAX}) …")
                time.sleep(wait)
                continue

            resp.raise_for_status()
            return resp

        except requests.exceptions.Timeout:
            if attempt == RETRY_MAX:
                raise
            wait = _jitter(RETRY_BACKOFF_BASE ** attempt)
            print(f"  [!] Timeout – thử lại sau {wait:.1f}s (lần {attempt}/{RETRY_MAX}) …")
            time.sleep(wait)

        except requests.exceptions.RequestException as e:
            if attempt == RETRY_MAX:
                raise
            wait = _jitter(RETRY_BACKOFF_BASE ** attempt)
            print(f"  [!] Lỗi: {e} – thử lại sau {wait:.1f}s (lần {attempt}/{RETRY_MAX}) …")
            time.sleep(wait)

    raise RuntimeError(f"Không thể tải URL sau {RETRY_MAX} lần: {url}")


def fetch_page(url: str) -> BeautifulSoup:
    """Tải trang HTML chính và trả về BeautifulSoup."""
    print(f"[*] Đang tải trang: {url}")
    resp = safe_request(url)
    resp.encoding = "utf-8"
    soup = BeautifulSoup(resp.text, "html.parser")
    print(f"[*] Tải trang xong – chờ {DELAY_PAGE}s …")
    time.sleep(DELAY_PAGE)
    return soup


def extract_metadata(soup: BeautifulSoup) -> dict:
    """Lấy tiêu đề, mô tả ngắn và thông tin cơ bản."""
    title = soup.find("h1", id="firstHeading")
    last_modified_tag = soup.find("li", id="footer-info-lastmod")
    return {
        "title": title.get_text(strip=True) if title else "",
        "url": URL,
        "last_modified": last_modified_tag.get_text(strip=True) if last_modified_tag else "",
    }


def extract_sections(soup: BeautifulSoup) -> list[dict]:
    """
    Trích xuất nội dung theo từng mục (heading + đoạn văn ngay dưới).
    Trả về danh sách: [{"level": 2, "heading": "...", "text": "..."}]
    """
    content = soup.find("div", id="mw-content-text")
    if not content:
        return []

    sections = []
    current = {"level": 1, "heading": "Giới thiệu", "paragraphs": []}

    for tag in content.find_all(["h2", "h3", "h4", "p", "ul", "ol"], recursive=True):
        if tag.name in ("h2", "h3", "h4"):
            # Lưu section cũ
            if current["paragraphs"]:
                current["text"] = "\n".join(current["paragraphs"])
                del current["paragraphs"]
                sections.append(current)
            level = int(tag.name[1])
            heading_text = tag.get_text(strip=True).replace("[sửa | sửa mã nguồn]", "").strip()
            current = {"level": level, "heading": heading_text, "paragraphs": []}
        elif tag.name == "p":
            text = tag.get_text(separator=" ", strip=True)
            if text:
                current["paragraphs"].append(text)
        elif tag.name in ("ul", "ol"):
            items = [li.get_text(separator=" ", strip=True) for li in tag.find_all("li", recursive=False)]
            if items:
                current["paragraphs"].append("\n".join(f"  • {i}" for i in items))

    # Section cuối
    if current["paragraphs"]:
        current["text"] = "\n".join(current["paragraphs"])
        del current["paragraphs"]
        sections.append(current)

    return sections


def extract_tables(soup: BeautifulSoup) -> list[dict]:
    """
    Trích xuất TẤT CẢ bảng trong trang.
    Mỗi bảng được lưu cả dạng list-of-dicts lẫn dạng raw rows.
    """
    tables_data = []
    all_tables = soup.find_all("table")
    print(f"[*] Tìm thấy {len(all_tables)} bảng")

    for idx, table in enumerate(all_tables):
        rows = []
        for tr in table.find_all("tr"):
            cells = []
            for cell in tr.find_all(["td", "th"]):
                # Lấy cả alt text của ảnh nếu có
                imgs = cell.find_all("img")
                img_alts = [img.get("alt", "") for img in imgs]
                cell_text = cell.get_text(separator=" ", strip=True)
                if img_alts:
                    cell_text = cell_text + " [img: " + ", ".join(img_alts) + "]"
                colspan = int(cell.get("colspan", 1))
                cells.extend([cell_text] * colspan)
            if cells:
                rows.append(cells)

        # Tạo caption
        caption_tag = table.find("caption")
        caption = caption_tag.get_text(strip=True) if caption_tag else f"Bảng {idx + 1}"

        # Chuyển thành records nếu có header
        records = []
        if len(rows) >= 2:
            header = rows[0]
            for row in rows[1:]:
                # Pad hoặc cắt để khớp header
                row_padded = row + [""] * (len(header) - len(row))
                records.append(dict(zip(header, row_padded[: len(header)])))

        tables_data.append({
            "table_index": idx,
            "caption": caption,
            "rows": rows,
            "records": records,
            "num_rows": len(rows),
            "num_cols": max((len(r) for r in rows), default=0),
        })

    return tables_data


def extract_images(soup: BeautifulSoup) -> list[dict]:
    """Thu thập metadata của tất cả hình ảnh (không tải về ngay)."""
    images = []
    for img in soup.find_all("img"):
        src = img.get("src", "")
        if not src:
            continue
        # Lấy URL đầy đủ
        if src.startswith("//"):
            src = "https:" + src
        elif src.startswith("/"):
            src = "https://vi.wikipedia.org" + src

        # Bỏ qua icon nhỏ (< 20px)
        width  = img.get("width",  "999")
        height = img.get("height", "999")
        try:
            if int(width) < 20 or int(height) < 20:
                continue
        except ValueError:
            pass

        images.append({
            "src": src,
            "alt": img.get("alt", ""),
            "width": width,
            "height": height,
            "filename": os.path.basename(urllib.parse.urlparse(src).path),
        })

    # Loại trùng theo src
    seen = set()
    unique = []
    for img in images:
        if img["src"] not in seen:
            seen.add(img["src"])
            unique.append(img)
    print(f"[*] Tìm thấy {len(unique)} ảnh duy nhất")
    return unique


def download_images(images: list[dict], image_dir: Path) -> list[dict]:
    """
    Tải ảnh về thư mục image_dir.
    - Dùng safe_request (retry + backoff) thay vì requests.get thô.
    - Delay có jitter giữa mỗi ảnh để tránh bị rate-limit.
    - Bỏ qua ảnh đã tồn tại (resume-friendly).
    """
    image_dir.mkdir(parents=True, exist_ok=True)
    failed = []

    for i, img in enumerate(images, 1):
        filename = img["filename"]

        # Xử lý tên file trùng
        local_path = image_dir / filename
        counter = 1
        while local_path.exists() and local_path.stat().st_size == 0:
            # File rỗng = lần trước tải lỗi → xoá và thử lại
            local_path.unlink()
            break
        if local_path.exists():
            img["local_path"] = str(local_path)
            print(f"  [{i}/{len(images)}] ↩ bỏ qua (đã có): {filename}")
            continue

        try:
            resp = safe_request(img["src"], timeout=25)
            local_path.write_bytes(resp.content)
            img["local_path"] = str(local_path)
            print(f"  [{i}/{len(images)}] ✓ {filename}")
        except Exception as e:
            img["local_path"] = None
            failed.append(filename)
            print(f"  [{i}/{len(images)}] ✗ {filename} – {e}")

        # Delay + jitter sau mỗi ảnh
        wait = _jitter(DELAY_BETWEEN_IMGS)
        time.sleep(wait)

    if failed:
        print(f"\n  [!] {len(failed)} ảnh thất bại sau tất cả retry:")
        for f in failed:
            print(f"      • {f}")

    return images


def save_tables_csv(tables: list[dict], output_dir: Path):
    """Lưu mỗi bảng ra một file CSV riêng."""
    csv_dir = output_dir / "tables_csv"
    csv_dir.mkdir(parents=True, exist_ok=True)
    for t in tables:
        if not t["rows"]:
            continue
        safe_name = re.sub(r"[^\w\-]", "_", t["caption"])[:60]
        path = csv_dir / f"table_{t['table_index']:03d}_{safe_name}.csv"
        with open(path, "w", newline="", encoding="utf-8-sig") as f:
            writer = csv.writer(f)
            writer.writerows(t["rows"])
    print(f"[*] Đã lưu {len(tables)} bảng CSV vào {csv_dir}")


def main():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    IMAGE_DIR.mkdir(parents=True, exist_ok=True)

    # 1. Tải trang
    soup = fetch_page(URL)

    # 2. Trích xuất dữ liệu
    print("[*] Đang trích xuất metadata …")
    metadata = extract_metadata(soup)

    print("[*] Đang trích xuất các mục nội dung …")
    sections = extract_sections(soup)
    print(f"    → {len(sections)} mục")

    print("[*] Đang trích xuất bảng …")
    tables = extract_tables(soup)

    print("[*] Đang thu thập metadata ảnh …")
    images = extract_images(soup)

    # 3. Hỏi người dùng có muốn tải ảnh không (mặc định: có)
    download = True  # Đặt False nếu không muốn tải ảnh
    if download:
        print("[*] Đang tải ảnh về …")
        images = download_images(images, IMAGE_DIR)
    else:
        print("[!] Bỏ qua tải ảnh (download=False)")

    # 4. Gộp toàn bộ vào JSON
    data = {
        "metadata": metadata,
        "sections": sections,
        "tables": tables,
        "images": images,
        "stats": {
            "num_sections": len(sections),
            "num_tables": len(tables),
            "num_images": len(images),
            "total_text_chars": sum(len(s.get("text", "")) for s in sections),
        },
    }

    json_path = OUTPUT_DIR / "bien_bao_data.json"
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"[✓] JSON → {json_path}  ({json_path.stat().st_size // 1024} KB)")

    # 5. Lưu bảng CSV
    save_tables_csv(tables, OUTPUT_DIR)

    # 6. Tóm tắt
    print("\n" + "=" * 55)
    print("  HOÀN THÀNH CRAWL")
    print("=" * 55)
    print(f"  Tiêu đề  : {metadata['title']}")
    print(f"  Mục      : {len(sections)}")
    print(f"  Bảng     : {len(tables)}")
    print(f"  Ảnh      : {len(images)}")
    print(f"  Thư mục  : {OUTPUT_DIR.resolve()}")
    print("=" * 55)


if __name__ == "__main__":
    main()