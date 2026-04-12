<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<nav class="navbar">
    <div class="navbar-brand">
        <div class="logo-box">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.42 10.922a1 1 0 0 0-.019-1.838L12.83 5.18a2 2 0 0 0-1.66 0L2.6 9.08a1 1 0 0 0 0 1.832l8.57 3.908a2 2 0 0 0 1.66 0z"></path><path d="M22 10v6"></path><path d="M6 12.5V16a6 3 0 0 0 12 0v-3.5"></path></svg>
        </div>
        <div>
            <div style="font-weight: 600; font-size: 14px; color: #1e3a8a;">Cổng thông tin Thí sinh</div>
            <div style="font-size: 11px; color: #6b7280;">Đại học Sài Gòn</div>
        </div>
    </div>

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/thisinh/dashboard" class="nav-item ${param.active == 'dashboard' ? 'active' : ''}">Trang chủ</a>
        <a href="${pageContext.request.contextPath}/thisinh/profile" class="nav-item ${param.active == 'profile' ? 'active' : ''}">Hồ sơ</a>
        <a href="${pageContext.request.contextPath}/thisinh/scores" class="nav-item ${param.active == 'scores' ? 'active' : ''}">Điểm thi</a>
        <a href="${pageContext.request.contextPath}/thisinh/wishes" class="nav-item ${param.active == 'wishes' ? 'active' : ''}">Nguyện vọng</a>
        <a href="${pageContext.request.contextPath}/thisinh/results" class="nav-item ${param.active == 'results' ? 'active' : ''}">Kết quả</a>
    </div>

    <div class="navbar-actions">
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-danger-outline">Đăng xuất</a>
    </div>
</nav>