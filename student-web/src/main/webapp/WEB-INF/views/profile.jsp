<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hồ sơ cá nhân - SGU</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/applicant.css">
    
    <style>
        .profile-header { display: flex; gap: 20px; align-items: center; border-bottom: 1px solid #f3f4f6; padding-bottom: 20px; margin-bottom: 24px; }
        .avatar-circle { width: 80px; height: 80px; background: linear-gradient(135deg, #8b5cf6, #3b82f6); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 32px; flex-shrink: 0; }
        
        /* BẢNG THÔNG TIN 2 CỘT */
        .info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
        .info-item { background: #f9fafb; padding: 14px 18px; border-radius: 8px; border: 1px solid #e5e7eb; }
        .info-item label { display: block; font-size: 12px; color: #6b7280; margin-bottom: 6px; text-transform: uppercase; font-weight: 600; letter-spacing: 0.5px;}
        .info-item span { font-size: 15px; color: #111827; font-weight: 500; word-break: break-word; }
        .info-item.full-width { grid-column: 1 / -1; }
        
        /* MODAL FIX VĂNG RA NGOÀI */
        .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); z-index: 9999; display: none; align-items: center; justify-content: center; backdrop-filter: blur(3px); }
        .modal-content { background: white; padding: 28px; border-radius: 12px; width: 90%; max-width: 550px; max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); }
        .form-group { margin-bottom: 16px; }
        .form-group label { display: block; font-size: 13px; margin-bottom: 6px; color: #374151; font-weight: 600;}
        .form-control { width: 100%; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; transition: all 0.2s; box-sizing: border-box; }
        .form-control:focus { border-color: #3b82f6; outline: none; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
        
        /* Ô Upload File */
        .upload-box { border: 2px dashed #cbd5e1; padding: 16px; text-align: center; border-radius: 8px; background: #f8fafc; transition: all 0.2s; }
        .upload-box:hover { border-color: #3b82f6; background: #eff6ff; }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"><jsp:param name="active" value="profile" /></jsp:include>

<main class="container">
    <div class="page-header">
        <h1 class="page-title">Thông tin cá nhân</h1>
        <p class="page-subtitle">Thông tin hồ sơ dự tuyển của bạn</p>
    </div>

    <c:if test="${param.success == 'true'}">
        <div style="background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; margin-bottom: 20px; padding: 16px; border-radius: 8px; font-weight: 500;">
            ✅ Đã gửi yêu cầu cập nhật hồ sơ! Vui lòng chờ bộ phận Tuyển sinh xét duyệt.
        </div>
    </c:if>

    <div class="card">
        <div class="profile-header">
            <div class="avatar-circle">👤</div>
            <div style="flex: 1;">
                <h2 style="margin: 0 0 4px 0; font-size: 20px; color: #111827;">${thongTin.ho} ${thongTin.ten}</h2>
                <p style="margin: 0 0 10px 0; font-size: 13px; color: #6b7280;">Mã số: TS2026${thongTin.id}</p>
                <span class="badge badge-success" style="background: #dcfce7; color: #16a34a; border: 1px solid #bbf7d0;">✓ Hồ sơ đang chờ duyệt</span>
            </div>
            <button type="button" class="btn btn-outline" onclick="document.getElementById('editModal').style.display='flex'">Yêu cầu chỉnh sửa</button>
        </div>

        <h3 style="font-size: 16px; margin: 0 0 16px 0; color: #111827;">Thông tin chi tiết</h3>
        
        <div class="info-grid">
            <div class="info-item"><label>Họ và tên</label><span>${thongTin.ho} ${thongTin.ten}</span></div>
            <div class="info-item"><label>Ngày sinh</label><span>${not empty thongTin.ngaySinh ? thongTin.ngaySinh : 'Chưa cập nhật'}</span></div>
            <div class="info-item"><label>Số CCCD</label><span style="font-family: monospace; color: #2563eb;">${thongTin.cccd}</span></div>
            <div class="info-item"><label>Giới tính</label><span><c:out value="${thongTin.gioiTinh}" default="Chưa cập nhật"/></span></div>
            <div class="info-item"><label>Số điện thoại</label><span><c:out value="${thongTin.dienThoai}" default="Chưa cập nhật"/></span></div>
            <div class="info-item"><label>Email</label><span><c:out value="${thongTin.email}" default="Chưa cập nhật"/></span></div>
            <div class="info-item full-width"><label>Nơi sinh</label><span><c:out value="${thongTin.noiSinh}" default="Chưa cập nhật"/></span></div>
            <div class="info-item"><label>Khu vực ưu tiên</label><span>KV <c:out value="${thongTin.khuVuc}" default="3"/></span></div>
            <div class="info-item"><label>Đối tượng ưu tiên</label><span>ĐT <c:out value="${thongTin.doiTuong}" default="Không"/></span></div>
        </div>
    </div>
        <div class="card">

        <h3 class="card-header">📁 Hồ sơ đính kèm</h3>

        

        <div class="doc-item doc-success">

            <div style="display: flex; gap: 12px; align-items: center;">

                <span style="color: #16a34a; font-size: 18px;">✓</span>

                <span style="font-size: 14px;">CCCD (scan 2 mặt)</span>

            </div>

            <span class="badge badge-success">Đã nộp</span>

        </div>

        

        <div class="doc-item doc-success">

            <div style="display: flex; gap: 12px; align-items: center;">

                <span style="color: #16a34a; font-size: 18px;">✓</span>

                <span style="font-size: 14px;">Bằng tốt nghiệp THPT</span>

            </div>

            <span class="badge badge-success">Đã nộp</span>

        </div>



        <div class="doc-item doc-warning">

            <div style="display: flex; gap: 12px; align-items: center;">

                <span style="color: #ca8a04; font-size: 18px;">⊗</span>

                <span style="font-size: 14px;">Giấy khai sinh</span>

            </div>

            <span class="badge badge-warning">Chưa nộp</span>

        </div>



        <div class="doc-item doc-success">

            <div style="display: flex; gap: 12px; align-items: center;">

                <span style="color: #16a34a; font-size: 18px;">✓</span>

                <span style="font-size: 14px;">Ảnh 3x4</span>

            </div>

            <span class="badge badge-success">Đã nộp</span>

        </div>

        

        <div class="alert-warning" style="margin-top: 16px;">

            💡 <strong>Lưu ý:</strong> Vui lòng nộp đầy đủ hồ sơ trước ngày 20/03/2026. Hồ sơ thiếu sẽ không được xét tuyển.

        </div>

    </div>
</main>

<div id="editModal" class="modal-overlay">
    <div class="modal-content">
        <h3 style="margin: 0 0 20px 0; font-size: 18px; color: #1e3a8a; border-bottom: 1px solid #e5e7eb; padding-bottom: 12px;">Gửi yêu cầu Cập nhật Hồ sơ</h3>
        
        <form action="${pageContext.request.contextPath}/thisinh/profile/update" method="POST" enctype="multipart/form-data">
            
            <div class="form-row">
                <div class="form-group">
                    <label>Số điện thoại mới</label>
                    <input type="text" name="dienThoai" class="form-control" value="${thongTin.dienThoai}">
                </div>
                <div class="form-group">
                    <label>Email mới</label>
                    <input type="email" name="email" class="form-control" value="${thongTin.email}">
                </div>
            </div>

            <div class="form-group">
                <label>Nơi sinh (Tỉnh/Thành phố)</label>
                <input type="text" name="noiSinh" class="form-control" value="${thongTin.noiSinh}">
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Khu vực ưu tiên</label>
                    <select name="khuVuc" id="khuVucSelect" class="form-control" onchange="toggleMinhChung()">
                        <option value="3" ${thongTin.khuVuc == '3' || empty thongTin.khuVuc ? 'selected' : ''}>KV3 (Không ưu tiên)</option>
                        <option value="2" ${thongTin.khuVuc == '2' ? 'selected' : ''}>KV2</option>
                        <option value="2NT" ${thongTin.khuVuc == '2NT' ? 'selected' : ''}>KV2-NT</option>
                        <option value="1" ${thongTin.khuVuc == '1' ? 'selected' : ''}>KV1</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Đối tượng ưu tiên</label>
                    <select name="doiTuong" id="doiTuongSelect" class="form-control" onchange="toggleMinhChung()">
                        <option value="Không" ${thongTin.doiTuong == 'Không' || empty thongTin.doiTuong ? 'selected' : ''}>Không ưu tiên</option>
                        <option value="01" ${thongTin.doiTuong == '01' ? 'selected' : ''}>ĐT 01</option>
                        <option value="06a" ${thongTin.doiTuong == '06a' ? 'selected' : ''}>ĐT 06a</option>
                    </select>
                </div>
            </div>

            <div class="form-group" id="minhChungGroup" style="display: none; background: #fffbeb; padding: 12px; border: 1px dashed #f59e0b; border-radius: 6px;">
                <label style="color: #b45309; font-weight: bold; margin-bottom: 8px;">⚠️ Tải lên ảnh/file Minh chứng (Bắt buộc)</label>
                <input type="file" name="fileMinhChung" id="fileMinhChung" class="form-control" accept="image/*,.pdf" style="background: white;">
                <p style="font-size: 11px; color: #6b7280; margin: 6px 0 0 0;">Hỗ trợ: JPG, PNG, PDF (Tối đa 5MB)</p>
            </div>

            <div style="display: flex; gap: 12px; justify-content: flex-end; margin-top: 24px; padding-top: 16px; border-top: 1px solid #f3f4f6;">
                <button type="button" class="btn btn-outline" onclick="document.getElementById('editModal').style.display='none'">Hủy bỏ</button>
                <button type="submit" class="btn btn-primary" style="background: #2563eb;">Gửi yêu cầu xét duyệt</button>
            </div>
        </form>
    </div>
</div>

<script>
    function toggleMinhChung() {
        var kv = document.getElementById("khuVucSelect").value;
        var dt = document.getElementById("doiTuongSelect").value;
        var group = document.getElementById("minhChungGroup");
        var fileInput = document.getElementById("fileMinhChung");
        
        // Nếu chọn KV1, KV2, KV2-NT HOẶC Đối tượng khác "Không" -> Hiển thị ô Upload
        if (kv !== "3" || (dt !== "Không" && dt !== "")) {
            group.style.display = "block";
            fileInput.required = true;
        } else {
            group.style.display = "none";
            fileInput.required = false;
        }
    }
    
    // check status
    window.onload = function() {
        toggleMinhChung();
    };
</script>

</body>
</html>