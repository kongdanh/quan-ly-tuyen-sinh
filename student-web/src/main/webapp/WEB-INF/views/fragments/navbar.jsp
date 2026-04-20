<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<style>
    /* CSS cho Nút Chuông và Dropdown Thông báo */
    .notif-wrapper { position: relative; display: flex; align-items: center; margin-right: 16px; }
    .notif-btn { background: none; border: none; cursor: pointer; position: relative; color: #4b5563; padding: 8px; border-radius: 50%; transition: 0.2s; }
    .notif-btn:hover { background: #f3f4f6; color: #1d4ed8; }
    .notif-badge { position: absolute; top: 2px; right: 2px; background: #ef4444; color: white; font-size: 10px; font-weight: bold; width: 16px; height: 16px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 2px solid white; }
    
    .notif-dropdown { display: none; position: absolute; top: 100%; right: 0; width: 340px; background: white; border-radius: 8px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); border: 1px solid #e5e7eb; z-index: 1000; margin-top: 8px; overflow: hidden; }
    .notif-header { padding: 12px 16px; font-weight: 600; color: #111827; border-bottom: 1px solid #e5e7eb; background: #f9fafb; font-size: 14px; }
    .notif-list { max-height: 350px; overflow-y: auto; }
    .notif-empty { padding: 24px; text-align: center; color: #6b7280; font-size: 13px; }
    
    .notif-item { padding: 12px 16px; border-bottom: 1px solid #f3f4f6; display: flex; gap: 12px; transition: 0.2s; }
    .notif-item:hover { background: #f9fafb; }
    .notif-icon { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 16px; }
    .notif-icon.success { background: #dcfce7; color: #16a34a; }
    .notif-icon.error { background: #fee2e2; color: #dc2626; }
    
    .notif-content { flex: 1; }
    .notif-title { font-size: 13px; font-weight: 600; margin-bottom: 4px; color: #111827; }
    .notif-desc { font-size: 12px; color: #4b5563; line-height: 1.4; }
    .notif-reason { margin-top: 6px; padding: 6px 10px; background: #fef2f2; border-left: 3px solid #ef4444; font-size: 11px; color: #991b1b; border-radius: 0 4px 4px 0; }
    .notif-time { font-size: 11px; color: #9ca3af; margin-top: 6px; display: block; }
</style>

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

    <div class="navbar-actions" style="display: flex; align-items: center;">
        
        <div class="notif-wrapper">
            <button class="notif-btn" onclick="toggleNotif(event)">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                
                <%-- Hiển thị chấm đỏ đếm số lượng nếu có thông báo --%>
                <c:if test="${not empty unreadCount and unreadCount > 0}">
                    <span class="notif-badge" id="notifBadge">${unreadCount}</span>
                </c:if>
            </button>

            <div class="notif-dropdown" id="notifDropdown">
                <div class="notif-header">Thông báo của bạn</div>
                <div class="notif-list">
                    
                    <c:choose>
                        <c:when test="${empty notifications}">
                            <div class="notif-empty">Chưa có thông báo nào.</div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="n" items="${notifications}">
                                <div class="notif-item">
                                    <div class="notif-icon ${n.trangThai == 'ACCEPTED' ? 'success' : 'error'}">
                                        ${n.trangThai == 'ACCEPTED' ? '✓' : '✗'}
                                    </div>
                                    
                                    <div class="notif-content">
                                        <div class="notif-title">Cập nhật trạng thái hồ sơ</div>
                                        <div class="notif-desc">
                                            Yêu cầu sửa đổi thông tin của bạn đã xử lý 
                                            <strong style="color: ${n.trangThai == 'ACCEPTED' ? '#16a34a' : '#dc2626'}">
                                                ${n.trangThai == 'ACCEPTED' ? 'THÀNH CÔNG' : 'THẤT BẠI'}
                                            </strong>.
                                        </div>
                                        
                                        <c:if test="${n.trangThai == 'REJECTED' && not empty n.note}">
                                            <div class="notif-reason">Lý do: ${n.note}</div>
                                        </c:if>
                                        
                                        <span class="notif-time">Mã YC: #${n.id}</span> 
                                    </div>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
        </div>

        <a href="${pageContext.request.contextPath}/logout" class="btn btn-danger-outline">Đăng xuất</a>
    </div>
</nav>

<script>
    function toggleNotif(event) {
        event.stopPropagation();
        var dropdown = document.getElementById('notifDropdown');
        var isOpening = (dropdown.style.display !== 'block');
        
        dropdown.style.display = isOpening ? 'block' : 'none';

        // TÍNH NĂNG ĐÁNH DẤU ĐÃ ĐỌC (SILENT UPDATE)
        var badge = document.getElementById('notifBadge');
        if (isOpening && badge) {
            // Gọi API ngầm xuống Servlet
            fetch('${pageContext.request.contextPath}/thisinh/notifications/read', {
                method: 'POST'
            }).then(response => {
                if (response.ok) {
                    badge.style.display = 'none';
                }
            }).catch(err => console.log("Lỗi đánh dấu đọc: ", err));
        }
    }

    window.onclick = function(event) {
        if (!event.target.closest('.notif-wrapper')) {
            var dropdown = document.getElementById('notifDropdown');
            if (dropdown && dropdown.style.display === 'block') {
                dropdown.style.display = 'none';
            }
        }
    }
</script>