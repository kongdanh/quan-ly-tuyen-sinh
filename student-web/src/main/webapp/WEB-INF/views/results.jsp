<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Kết quả xét tuyển - SGU</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/applicant.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"><jsp:param name="active" value="results" /></jsp:include>

<main class="container">
    <div class="page-header">
        <h1 class="page-title">Kết quả Xét tuyển</h1>
        <p class="page-subtitle">Hệ thống tuyển sinh Đại học Sài Gòn 2026</p>
    </div>

    <c:choose>
        <%-- TRƯỜNG HỢP 1: ĐÃ CÔNG BỐ VÀ TRÚNG TUYỂN --%>
        <c:when test="${daCoKetQua && not empty trungTuyen}">
            <div class="result-banner success">
                <div style="font-size: 40px; margin-bottom: 12px;">✅</div>
                <h2 style="font-size: 32px; margin: 0 0 8px 0; font-weight: 700;">CHÚC MỪNG BẠN!</h2>
                <p style="font-size: 18px; margin: 0 0 24px 0;">Bạn đã đủ điều kiện TRÚNG TUYỂN</p>
                <div style="display: inline-block; background: rgba(255,255,255,0.2); padding: 10px 24px; border-radius: 8px; font-size: 16px;">
                    Ngành: <strong>${trungTuyen.nganh.tennganh}</strong>
                </div>
            </div>
            </c:when>

        <%-- TRƯỜNG HỢP 2: ĐÃ CÔNG BỐ NHƯNG RỚT HẾT --%>
        <c:when test="${daCoKetQua && empty trungTuyen}">
            <div class="result-banner" style="background: linear-gradient(135deg, #ef4444, #991b1b); color: white;">
                <div style="font-size: 40px; margin-bottom: 12px;">😔</div>
                <h2 style="font-size: 28px; margin: 0 0 8px 0; font-weight: 700;">CHƯA ĐẠT KẾT QUẢ</h2>
                <p style="font-size: 16px; margin: 0;">Rất tiếc, điểm xét tuyển của bạn chưa đủ điều kiện trúng tuyển vào các NV đã đăng ký.</p>
            </div>
        </c:when>

        <%-- TRƯỜNG HỢP 3: ĐANG CHỜ KẾT QUẢ TỪ ADMIN  --%>
        <c:otherwise>
            <div class="card" style="text-align: center; padding: 60px 20px; background: white; border: 2px dashed #e5e7eb;">
                <div style="width: 80px; height: 80px; background: #eff6ff; color: #3b82f6; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 36px; margin: 0 auto 20px auto;">
                    ⏳
                </div>
                <h2 style="font-size: 22px; color: #111827; margin: 0 0 8px 0;">Đang chờ xử lý kết quả</h2>
                <p style="color: #6b7280; font-size: 15px; margin: 0;">Hội đồng Tuyển sinh đang tiến hành đối soát và xét duyệt.<br>Vui lòng quay lại sau khi có thông báo chính thức (Dự kiến 25/08/2026).</p>
            </div>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>