<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Cổng thông tin Thí sinh 2026</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
    
    <style>
        .password-box { position: relative; display: flex; align-items: center; }
        .password-box input { width: 100%; padding-right: 40px; }
        .toggle-btn { 
            position: absolute; right: 10px; background: none; border: none; 
            cursor: pointer; padding: 0; display: flex; align-items: center; justify-content: center;
            color: #6b7280; transition: color 0.2s;
        }
        .toggle-btn:hover { color: #2563eb; }
        .toggle-btn svg { width: 20px; height: 20px; }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="logo-placeholder">🎓</div>
        <h2>Cổng Thí Sinh 2026</h2>
        <p class="subtitle">Hệ thống xét tuyển Đại học Sài Gòn</p>
        
        <%-- Error handling --%>
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert">
                <strong>Lỗi:</strong> <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <form action="${pageContext.request.contextPath}/LoginServlet" method="POST" class="login-form">
            <div class="form-group">
                <label for="cccd">Số CCCD / Mã định danh</label>
                <input type="text" id="cccd" name="cccd" placeholder="Nhập số căn cước hoặc mã định danh" required autofocus>
            </div>
            
            <div class="form-group">
                <label for="password">Mật khẩu</label>
                <div class="password-box">
                    <input type="password" id="password" name="password" placeholder="Nhập mật khẩu" required>
                    <button type="button" class="toggle-btn" onclick="togglePass()" title="Hiện/Ẩn mật khẩu">
                        <svg id="eye-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                    </button>
                </div>
            </div>
            
            <button type="submit" id="submitBtn">Đăng Nhập</button>
        </form>
        
        <div class="note-box">
            <p>Mật khẩu mặc định là <strong>ngày sinh</strong> của bạn.</p>
            <p>Định dạng: <strong>dd/mm/yyyy</strong> (Ví dụ: 15/03/2007)</p>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>