import requests
import os
import time

# 1. Tạo thư mục để lưu ảnh (nếu chưa có)
thumuc_luu = "anh_cau_hoi_xa_hinh_a1"
if not os.path.exists(thumuc_luu):
    os.makedirs(thumuc_luu)

# Header giả lập trình duyệt để tránh bị server chặn (HTTP 403 Forbidden)
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
}

# 2. Vòng lặp từ câu 1 đến câu 601
for i in range(1, 602):
    # Tạo đường link từ hoclaixehcm.vn
    url = f"https://hoclaixehcm.vn/static/data/images/a1/{i}.jpg"
    
    try:
        # Gửi yêu cầu GET để lấy ảnh (không theo dõi redirect tự động)
        response = requests.get(url, headers=headers, timeout=10, allow_redirects=False)
        
        # Kiểm tra nếu link bị chuyển hướng (redirect) thì bỏ qua
        if 300 <= response.status_code < 400:
            print(f"[Bỏ qua] Câu {i} bị chuyển hướng (Mã: {response.status_code}) - URL: {url}")
        # Kiểm tra xem link có tồn tại không (HTTP 200 là thành công)
        elif response.status_code == 200:
            # Đường dẫn file sẽ lưu trên máy tính
            duong_dan_file = os.path.join(thumuc_luu, f"{i}.jpg")
            
            # Ghi dữ liệu ảnh vào file
            with open(duong_dan_file, 'wb') as file:
                file.write(response.content)
            print(f"[Thành công] Đã tải Câu {i}")
        else:
            print(f"[Thất bại] Không tìm thấy Câu {i} (Mã lỗi: {response.status_code}) - URL: {url}")
            
    except Exception as e:
        print(f"[Lỗi] Câu {i}: {e}")
    
    # Dừng 0.5 giây giữa các lần tải để tránh việc server nhận diện là tấn công DDOS và chặn IP của bạn
    time.sleep(0.5)

print("Đã hoàn tất quá trình cào dữ liệu!")