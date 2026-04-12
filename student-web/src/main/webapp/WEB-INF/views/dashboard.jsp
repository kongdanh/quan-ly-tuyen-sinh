<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Dashboard - Hệ thống Xét tuyển SGU</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/applicant.css">
    <style>
        .banner-gradient {
            background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%) !important;
            color: white !important;
            border: none !important;
            padding: 36px 32px !important;
        }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"><jsp:param name="active" value="dashboard" /></jsp:include>

<main class="container">
    <c:if test="${isDefaultPassword}">
        <div class="alert-box" style="background: #fffbeb; border: 1px solid #fcd34d; color: #b45309; display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
            <div style="font-size: 14px;">
                ⚠️ <strong>Cảnh báo bảo mật:</strong> Tài khoản của bạn đang sử dụng mật khẩu mặc định (Ngày sinh). Để tránh rủi ro mất tài khoản, vui lòng thay đổi mật khẩu ngay!
            </div>
            <button type="button" class="btn" style="background: #ea580c; color: white; border: none; font-size: 13px; font-weight: 600; padding: 8px 16px; border-radius: 6px; cursor: pointer;" onclick="document.getElementById('pwdModal').style.display='flex'">Đổi mật khẩu ngay</button>
        </div>
    </c:if>

    <c:if test="${param.success == 'pwd_changed'}">
        <div class="alert-box" style="background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; margin-bottom: 24px; padding: 12px 16px; border-radius: 8px; font-weight: 500;">✅ Chúc mừng! Bạn đã đổi mật khẩu thành công.</div>
    </c:if>
    <c:if test="${param.error == 'wrong_old_pwd'}">
        <div class="alert-box" style="background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca; margin-bottom: 24px; padding: 12px 16px; border-radius: 8px; font-weight: 500;">❌ Đổi mật khẩu thất bại: Mật khẩu hiện tại không chính xác!</div>
    </c:if>
    <c:if test="${param.error == 'pwd_mismatch'}">
        <div class="alert-box" style="background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca; margin-bottom: 24px; padding: 12px 16px; border-radius: 8px; font-weight: 500;">❌ Đổi mật khẩu thất bại: Mật khẩu xác nhận không khớp!</div>
    </c:if>
    <div class="card banner-gradient">
        <h1 style="font-size: 28px; margin: 0 0 8px 0; font-weight: 600;">Xin chào, ${user.hoTen}!</h1>
        <p style="color: #dbeafe; margin: 0 0 20px 0; font-size: 15px;">Mã thí sinh: <span style="font-family: monospace;">${user.idThiSinh}</span></p>
        <div style="display: inline-block; background: rgba(255,255,255,0.15); padding: 8px 16px; border-radius: 8px; font-size: 14px; backdrop-filter: blur(4px);">
            ⏳ Trạng thái: Đang nộp hồ sơ
        </div>
    </div>

    <div class="grid-3">
<div class="card" onclick="window.location.href='${pageContext.request.contextPath}/thisinh/scores'" style="cursor: pointer; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
                    <div class="icon-box icon-blue"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg></div>
                    <span class="badge badge-success" style="background: #dcfce7; color: #16a34a; font-weight: normal; border: 1px solid #bbf7d0;">Đã có</span>
                </div>
                <h3 style="margin: 0 0 8px 0; font-size: 16px; color: #111827;">Điểm thi của tôi</h3>
                <p style="font-size: 13px; color: #6b7280; margin: 0 0 16px 0;">Tổng điểm xét tuyển</p>
                <p style="color: #2563eb; font-size: 32px; font-weight: bold; margin: 0 0 16px 0;">${maxToHop != null ? maxToHop.totalStr : '0.00'}</p>
            </div>
            <p style="font-size: 12px; color: #6b7280; margin: 0; padding-top: 16px; border-top: 1px solid #f3f4f6;">
                ${maxToHop != null ? maxToHop.desc : 'Chưa có đủ điểm tổ hợp'}
            </p>
        </div>

        <div class="card" onclick="window.location.href='${pageContext.request.contextPath}/thisinh/wishes'" style="cursor: pointer;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
                <div style="font-size: 24px;">🎯</div>
                <c:choose>
                    <c:when test="${not empty wishes}"><span class="badge badge-info">Đã đăng ký</span></c:when>
                    <c:otherwise><span class="badge badge-warning">Chưa đăng ký</span></c:otherwise>
                </c:choose>
            </div>
            <h3 style="margin: 0 0 4px 0; font-size: 16px;">Nguyện vọng</h3>
            <p class="text-muted" style="font-size: 13px; margin: 0 0 12px 0;">Đã đăng ký ${wishes.size()} nguyện vọng</p>
            <ul style="list-style: none; padding: 0; margin: 0; font-size: 13px;">
                <c:forEach var="w" items="${wishes}" end="1">
                    <li style="margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"><span class="text-muted">NV${w.nvTt}:</span> ${w.nganh.tennganh}</li>
                </c:forEach>
            </ul>
        </div>

        <div class="card" onclick="window.location.href='${pageContext.request.contextPath}/thisinh/results'" style="cursor: pointer;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
                <div style="font-size: 24px;">🏆</div>
                <span class="badge badge-warning">Đang chờ</span>
            </div>
            <h3 style="margin: 0 0 4px 0; font-size: 16px;">Kết quả xét tuyển</h3>
            <p class="text-muted" style="font-size: 13px; margin: 0 0 12px 0;">Công bố dự kiến</p>
            <p style="font-size: 20px; font-weight: bold; margin: 0; color: #111827;">25/08/2026</p>
        </div>
    </div>
</main>
<div id="pwdModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); z-index: 9999; align-items: center; justify-content: center; backdrop-filter: blur(3px);">
    <div style="background: white; padding: 28px; border-radius: 12px; width: 90%; max-width: 400px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1);">
        <h3 style="margin: 0 0 20px 0; font-size: 18px; color: #111827; border-bottom: 1px solid #e5e7eb; padding-bottom: 12px;">Đổi Mật Khẩu</h3>
        
        <form action="${pageContext.request.contextPath}/thisinh/change-password" method="POST" onsubmit="return validatePwdForm()">
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 13px; margin-bottom: 6px; color: #374151; font-weight: 600;">Mật khẩu hiện tại</label>
                <input type="password" name="oldPassword" required style="width: 100%; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; font-size: 14px;">
            </div>
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 13px; margin-bottom: 6px; color: #374151; font-weight: 600;">Mật khẩu mới</label>
                <input type="password" name="newPassword" id="newPassword" required minlength="6" style="width: 100%; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; font-size: 14px;">
            </div>
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 13px; margin-bottom: 6px; color: #374151; font-weight: 600;">Xác nhận mật khẩu mới</label>
                <input type="password" name="confirmPassword" id="confirmPassword" required minlength="6" style="width: 100%; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; font-size: 14px;">
                <p id="pwdErrorMsg" style="color: #ef4444; font-size: 12px; margin-top: 6px; display: none;">Mật khẩu xác nhận không khớp!</p>
            </div>
            
            <div style="display: flex; gap: 12px; justify-content: flex-end; margin-top: 24px; padding-top: 16px; border-top: 1px solid #f3f4f6;">
                <button type="button" onclick="document.getElementById('pwdModal').style.display='none'" style="padding: 8px 16px; border: 1px solid #d1d5db; background: white; border-radius: 6px; cursor: pointer; color: #374151;">Hủy bỏ</button>
                <button type="submit" style="padding: 8px 16px; background: #2563eb; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: 500;">Cập nhật mật khẩu</button>
            </div>
        </form>
    </div>
</div>

<script>
    function validatePwdForm() {
        var newP = document.getElementById("newPassword").value;
        var confP = document.getElementById("confirmPassword").value;
        if (newP !== confP) {
            document.getElementById("pwdErrorMsg").style.display = "block";
            return false;
        }
        document.getElementById("pwdErrorMsg").style.display = "none";
        return true;
    }
</script>
</body>
</html>