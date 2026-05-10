#!/usr/bin/env python3
"""
Convert questions.js → firebase_data.json
Mapping sang CauHoi model của Android app.

MaLoaiCH mapping (dựa theo RandomQuizz dùng MaLoaiCH=1 cho 2 câu điểm liệt):
  1 = diem-liet  (điểm liệt - thi sai là rớt ngay)
  2 = khai-niem
  3 = bien-bao
  4 = tinh-huong
  5 = ky-thuat
  6 = cau-tao
  7 = van-hoa

MaLoaiBang: 1 = A1, 2 = B1
"""

import re
import json
import os

CATEGORY_MAP = {
    'diem-liet': 1,
    'khai-niem': 2,
    'bien-bao':  3,
    'tinh-huong': 4,
    'ky-thuat':  5,
    'cau-tao':   6,
    'van-hoa':   7,
}

# Thay đổi giá trị này nếu câu hỏi dành cho hạng khác
# 1 = A1,  2 = B1
MA_LOAI_BANG = 2


def get_ma_loai_ch(category_str):
    cats = category_str.strip().split()
    # diem-liet được ưu tiên (MaLoaiCH=1 dùng cho RandomQuizz)
    if 'diem-liet' in cats:
        return 1
    for cat in cats:
        if cat in CATEGORY_MAP:
            return CATEGORY_MAP[cat]
    return 2


def get_hinh_anh(hinhanhq):
    if not hinhanhq:
        return ""
    # "/img/600cau2025/301.webp" → "301.webp"
    return os.path.basename(hinhanhq)


def get_dap_an_dung(answers):
    for i, ans in enumerate(answers[:4]):
        if ans.get('correct'):
            return chr(ord('A') + i)  # 0→A, 1→B, 2→C, 3→D
    return 'A'


def parse_questions_js(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Bỏ "const questions = "
    content = re.sub(r'^\s*const\s+\w+\s*=\s*', '', content)
    content = content.rstrip().rstrip(';').strip()

    # Pass 1: keys ở đầu dòng (indent + word + colon)
    content = re.sub(
        r'^(\s*)([a-zA-Z_][a-zA-Z0-9_]*)(\s*:)',
        lambda m: m.group(1) + '"' + m.group(2) + '"' + m.group(3),
        content,
        flags=re.MULTILINE
    )

    # Pass 2: keys inline sau { hoặc , (ví dụ: { text: "...", correct: false })
    content = re.sub(
        r'([{,])\s*([a-zA-Z_][a-zA-Z0-9_]*)(\s*:)',
        lambda m: m.group(1) + ' "' + m.group(2) + '"' + m.group(3),
        content
    )

    # Xoá trailing commas trước } hoặc ]
    content = re.sub(r',(\s*[}\]])', r'\1', content)

    return json.loads(content)


def convert(questions):
    cauhoi_node = {}

    for q in questions:
        num = q['number']
        answers = q.get('answers', [])

        dap_an = ['', '', '', '']
        for i, ans in enumerate(answers[:4]):
            dap_an[i] = ans.get('text', '')

        cauhoi_node[str(num)] = {
            "MaCH":        num,
            "MaLoaiCH":    get_ma_loai_ch(q.get('category', 'khai-niem')),
            "MaLoaiBang":  MA_LOAI_BANG,
            "NoiDung":     q.get('question', ''),
            "HinhAnh":     get_hinh_anh(q.get('hinhanhq', '')),
            "DapAnA":      dap_an[0],
            "DapAnB":      dap_an[1],
            "DapAnC":      dap_an[2],
            "DapAnD":      dap_an[3],
            "DapAnDung":   get_dap_an_dung(answers),
            "GiaiThich":   q.get('explanation', ''),
            "HaySai":      0,
            "Luu":         0,
            "DaTraLoiDung": 0,
        }

    return cauhoi_node


def main():
    base = os.path.dirname(os.path.abspath(__file__))
    input_path  = os.path.join(base, 'app/src/main/assets/questions.js')
    output_path = os.path.join(base, 'firebase_data.json')

    print("Đang parse questions.js ...")
    questions = parse_questions_js(input_path)
    print(f"Đã parse: {len(questions)} câu hỏi")

    print("Đang convert sang Firebase format ...")
    cauhoi_node = convert(questions)

    firebase_data = {
        "Version": 1,
        "LoaiCauHoi": {
            "1": {"MaLoaiCH": 1, "TenLoaiCauHoi": "Điểm liệt"},
            "2": {"MaLoaiCH": 2, "TenLoaiCauHoi": "Khái niệm"},
            "3": {"MaLoaiCH": 3, "TenLoaiCauHoi": "Biển báo"},
            "4": {"MaLoaiCH": 4, "TenLoaiCauHoi": "Tình huống"},
            "5": {"MaLoaiCH": 5, "TenLoaiCauHoi": "Kỹ thuật"},
            "6": {"MaLoaiCH": 6, "TenLoaiCauHoi": "Cấu tạo"},
            "7": {"MaLoaiCH": 7, "TenLoaiCauHoi": "Văn hóa"},
        },
        "Loaibang": {
            "1": {"MaLoaiBang": 1, "TenLoaiBang": "A1"},
            "2": {"MaLoaiBang": 2, "TenLoaiBang": "B1"},
        },
        "CauHoi": cauhoi_node,
    }

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(firebase_data, f, ensure_ascii=False, indent=2)

    print(f"\nĐã xuất: {output_path}")
    print(f"Kích thước file: {os.path.getsize(output_path) / 1024:.1f} KB")

    # Thống kê
    cat_names = {
        1: 'Điểm liệt', 2: 'Khái niệm', 3: 'Biển báo',
        4: 'Tình huống', 5: 'Kỹ thuật', 6: 'Cấu tạo', 7: 'Văn hóa',
    }
    by_cat = {}
    has_img = 0
    for q in cauhoi_node.values():
        c = q['MaLoaiCH']
        by_cat[c] = by_cat.get(c, 0) + 1
        if q['HinhAnh']:
            has_img += 1

    print("\nPhân bố theo loại câu hỏi (MaLoaiCH):")
    for k in sorted(by_cat):
        print(f"  [{k}] {cat_names[k]:<12}: {by_cat[k]} câu")

    print(f"\nCâu có ảnh (HinhAnh != ''): {has_img}")
    print(f"Câu không có ảnh           : {len(cauhoi_node) - has_img}")


if __name__ == '__main__':
    main()
