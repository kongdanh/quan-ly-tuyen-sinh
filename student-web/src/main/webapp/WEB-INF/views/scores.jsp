<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Điểm thi của tôi - SGU</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/applicant.css">
    <style>
        .score-row { display: flex; justify-content: space-between; padding: 14px 16px; border-bottom: 1px solid #f3f4f6; }
        .score-row:last-child { border-bottom: none; }
        .score-label { font-size: 14px; color: #4b5563; font-weight: 500; }
        .score-value { font-size: 15px; font-weight: 700; color: #111827; }
        
        .tohop-card { padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb; margin-bottom: 12px; background: white; display: flex; justify-content: space-between; align-items: center; transition: all 0.2s;}
        .tohop-card:hover { border-color: #bfdbfe; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
        .tohop-highest { background: #eff6ff; border-color: #93c5fd; border-left: 4px solid #2563eb; }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"><jsp:param name="active" value="scores" /></jsp:include>

<main class="container">
    <div class="page-header">
        <h1 class="page-title">Điểm thi của tôi</h1>
        <p class="page-subtitle">Phương thức xét tuyển: <strong>${diem != null ? diem.DPhuongthuc : 'Chưa có dữ liệu'}</strong></p>
    </div>

    <div class="card bg-purple-gradient" style="padding: 32px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <p class="text-light-blue" style="font-size: 14px; margin: 0 0 8px 0; text-transform: uppercase; font-weight: 600;">
                    Tổ hợp tối ưu nhất <c:if test="${not empty maxToHop}">(${maxToHop.name})</c:if>
                </p>
                <p class="text-white" style="font-size: 48px; font-weight: 700; margin: 0;">${maxToHop != null ? maxToHop.totalStr : '0.00'}</p>
                <p class="text-light-blue" style="font-size: 14px; margin: 8px 0 0 0;">
                    ${maxToHop != null ? maxToHop.desc : 'Đang cập nhật dữ liệu...'}
                </p>
            </div>
            <div style="font-size: 48px; opacity: 0.8;">🎯</div>
        </div>
    </div>

    <div class="grid-2">
        <div class="card" style="padding: 0; overflow: hidden;">
            <div style="padding: 20px 24px; background: #f9fafb; border-bottom: 1px solid #e5e7eb;">
                <h3 style="margin: 0; font-size: 16px; color: #111827;">Các môn đã có điểm</h3>
            </div>
            <div style="padding: 8px;">
                <c:if test="${not empty diem.to and diem.to > 0}"><div class="score-row"><span class="score-label">Toán học</span><span class="score-value">${diem.to}</span></div></c:if>
                <c:if test="${not empty diem.li and diem.li > 0}"><div class="score-row"><span class="score-label">Vật lý</span><span class="score-value">${diem.li}</span></div></c:if>
                <c:if test="${not empty diem.ho and diem.ho > 0}"><div class="score-row"><span class="score-label">Hóa học</span><span class="score-value">${diem.ho}</span></div></c:if>
                <c:if test="${not empty diem.va and diem.va > 0}"><div class="score-row"><span class="score-label">Ngữ văn</span><span class="score-value">${diem.va}</span></div></c:if>
                <c:if test="${not empty diem.su and diem.su > 0}"><div class="score-row"><span class="score-label">Lịch sử</span><span class="score-value">${diem.su}</span></div></c:if>
                <c:if test="${not empty diem.di and diem.di > 0}"><div class="score-row"><span class="score-label">Địa lý</span><span class="score-value">${diem.di}</span></div></c:if>
                <c:if test="${not empty diem.si and diem.si > 0}"><div class="score-row"><span class="score-label">Sinh học</span><span class="score-value">${diem.si}</span></div></c:if>
                <c:if test="${not empty diem.n1Thi and diem.n1Thi > 0}"><div class="score-row"><span class="score-label">Ngoại ngữ</span><span class="score-value">${diem.n1Thi}</span></div></c:if>
                <c:if test="${not empty diem.ktpl and diem.ktpl > 0}"><div class="score-row"><span class="score-label">Giáo dục CD (KTPL)</span><span class="score-value">${diem.ktpl}</span></div></c:if>
            </div>
        </div>

        <div class="card" style="background: transparent; border: none; padding: 0; box-shadow: none;">
            <h3 style="font-size: 16px; color: #111827; margin: 0 0 16px 0;">Danh sách tổ hợp khả dụng</h3>
            
            <c:choose>
                <c:when test="${not empty listToHop}">
                    <c:forEach var="th" items="${listToHop}">
                        <div class="tohop-card ${th.name == maxToHop.name ? 'tohop-highest' : ''}">
                            <div>
                                <div style="font-weight: 600; color: #111827; margin-bottom: 4px; font-size: 15px;">
                                    Tổ hợp ${th.name}
                                    <c:if test="${th.name == maxToHop.name}"><span class="badge badge-success" style="padding: 2px 8px; font-size: 11px; margin-left: 8px;">Tối ưu nhất</span></c:if>
                                </div>
                                <div style="font-size: 12px; color: #6b7280; margin-bottom: 2px;">${th.desc}</div>
                                <div style="font-size: 12px; color: #9ca3af; font-family: monospace;">[ ${th.detail} ]</div>
                            </div>
                            <div style="font-size: 20px; font-weight: bold; color: ${th.name == maxToHop.name ? '#2563eb' : '#374151'};">
                                ${th.totalStr}
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="alert-warning" style="background: white;">Chưa có đủ điểm 3 môn để tạo thành tổ hợp xét tuyển.</div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>
</body>
</html>