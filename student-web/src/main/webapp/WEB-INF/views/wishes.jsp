<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Nguyện vọng - SGU</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/applicant.css">
    
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet" />
    <script src="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/js/select2.min.js"></script>
    
    <script src="https://cdn.jsdelivr.net/npm/sortablejs@latest/Sortable.min.js"></script>

    <style>
        .wish-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); overflow: hidden; transition: transform 0.2s; }
        .wish-card.sortable-ghost { opacity: 0.4; transform: scale(0.98); } 
        .wish-header { background: #f9fafb; padding: 16px 24px; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; }
        .wish-body { padding: 24px; display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
        
        .drag-handle { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; font-size: 20px; color: #9ca3af; cursor: grab; margin-right: 12px; transition: color 0.2s; }
        .drag-handle:hover { color: #2563eb; }
        .drag-handle:active { cursor: grabbing; }

        .wish-rank { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; font-weight: bold; font-size: 15px; margin-right: 12px; }
        .rank-1 { background: #fef08a; color: #854d0e; } 
        .rank-other { background: #dbeafe; color: #1e40af; }
        
        .data-box { background: #f9fafb; border: 1px solid #e5e7eb; padding: 10px 14px; border-radius: 6px; font-size: 14px; color: #111827; font-weight: 500; margin-top: 6px; }
        .data-highlight { background: #f0fdf4; border-color: #bbf7d0; color: #16a34a; }
        
        .modal-overlay { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center; backdrop-filter: blur(2px); }
        .modal-content { background: white; padding: 24px; border-radius: 12px; width: 100%; max-width: 500px; overflow: visible; } /* Phải để visible để Select2 xổ xuống không bị cắt */
        
        .select2-container .select2-selection--single { height: 42px; border: 1px solid #d1d5db; border-radius: 6px; display: flex; align-items: center; }
        .select2-container--default .select2-selection--single .select2-selection__rendered { line-height: 42px; font-size: 14px; }
        .select2-container--default .select2-selection--single .select2-selection__arrow { height: 40px; }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"><jsp:param name="active" value="wishes" /></jsp:include>

<main class="container">
    <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24px;">
        <div>
            <h1 class="page-title">Đăng ký Nguyện vọng</h1>
            <p class="page-subtitle">Bạn đã đăng ký <strong>${wishes.size()}</strong> nguyện vọng xét tuyển. <span style="color:#2563eb;">(Nắm và kéo biểu tượng ☰ để thay đổi thứ tự)</span></p>
        </div>
        <c:if test="${wishes.size() < 3}">
            <button class="btn btn-primary" style="background: #16a34a;" onclick="openWishModal('add')">+ Thêm nguyện vọng</button>
        </c:if>
    </div>

    <c:if test="${param.success == 'add'}"><div class="alert-box" style="background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; margin-bottom: 16px; border-radius: 8px; padding: 12px;">✅ Lưu nguyện vọng thành công! Hệ thống đã tự động tính tổ hợp tối ưu nhất.</div></c:if>
    <c:if test="${param.success == 'delete'}"><div class="alert-box" style="background: #e0e7ff; color: #1d4ed8; border: 1px solid #bfdbfe; margin-bottom: 16px; border-radius: 8px; padding: 12px;">🗑 Đã xóa nguyện vọng và sắp xếp lại thứ tự thành công!</div></c:if>
    <c:if test="${param.error == 'not_qualified'}"><div class="alert-box" style="background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca; margin-bottom: 16px; border-radius: 8px; padding: 12px;">❌ Bạn chưa có đủ điểm 3 môn hoặc bị điểm liệt để xét tuyển vào ngành này!</div></c:if>

    <c:set var="hasWarning" value="false" />
    <c:if test="${wishes.size() > 1}">
        <c:forEach var="i" begin="0" end="${wishes.size() - 2}">
            <c:if test="${wishes[i].nganh.NDiemsan < wishes[i+1].nganh.NDiemsan}">
                <c:set var="hasWarning" value="true" />
            </c:if>
        </c:forEach>
    </c:if>
    <c:if test="${hasWarning}">
        <div class="alert-box" style="background: #fffbeb; border: 1px solid #fcd34d; color: #b45309; margin-bottom: 20px; padding: 12px; border-radius: 8px;">
            ⚠️ <strong>Cảnh báo Logic:</strong> Nguyện vọng có điểm sàn thấp đang xếp trên nguyện vọng điểm cao. Việc này có thể làm mất cơ hội trúng tuyển của bạn. Hãy kéo thả để đổi lại thứ tự!
        </div>
    </c:if>

    <div id="wishes-container">
        <c:choose>
            <c:when test="${not empty wishes}">
                <c:forEach var="wish" items="${wishes}" varStatus="status">
                    <div class="wish-card" data-id="${wish.id}">
                        <div class="wish-header">
                            <div style="display: flex; align-items: center;">
                                <div class="drag-handle" title="Kéo để đổi thứ tự">☰</div>
                                
                                <div class="wish-rank ${status.index == 0 ? 'rank-1' : 'rank-other'}">${status.index + 1}</div>
                                <div>
                                    <h3 style="margin: 0 0 2px 0; font-size: 16px; color: #111827;">Nguyện vọng ${status.index + 1}</h3>
                                    <span style="font-size: 12px; color: #6b7280;">Trạng thái: <strong style="color: #ea580c;">Đang xử lý</strong></span>
                                </div>
                            </div>
                            <div style="display: flex; gap: 8px;">
                                <button class="btn btn-outline" style="padding: 6px 12px; font-size: 13px;" onclick="openWishModal('edit', '${wish.id}', '${wish.nganh.manganh}')">✏️ Sửa</button>
                                <form action="${pageContext.request.contextPath}/thisinh/wishes/delete" method="POST" style="margin: 0;">
                                    <input type="hidden" name="idnv" value="${wish.id}">
                                    <button type="submit" class="btn btn-outline" style="padding: 6px 12px; font-size: 13px; color: #ef4444;" onclick="return confirm('Bạn có chắc chắn muốn xóa nguyện vọng này?');">🗑 Xóa</button>
                                </form>
                            </div>
                        </div>
                        <div class="wish-body">
                            <div><label style="font-size: 13px; color: #4b5563;">Ngành đăng ký</label><div class="data-box">${wish.nganh.tennganh} (${wish.nganh.manganh})</div></div>
                            <div><label style="font-size: 13px; color: #4b5563;">Tổ hợp xét tuyển</label><div class="data-box">${not empty wish.ttThm ? wish.ttThm : 'Đang cập nhật'}</div></div>
                            <div><label style="font-size: 13px; color: #4b5563;">Điểm xét của bạn</label><div class="data-box data-highlight">${not empty wish.diemXettuyen ? wish.diemXettuyen : 'Chưa có'}</div></div>
                            <div><label style="font-size: 13px; color: #4b5563;">Điểm sàn của ngành</label><div class="data-box" style="background: #fffbeb; border-color: #fef3c7; color: #b45309;">${wish.nganh.NDiemsan}</div></div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="alert-box alert-warning">Bạn chưa đăng ký nguyện vọng nào trên hệ thống! Vui lòng bấm nút Thêm nguyện vọng.</div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<div id="wishModal" class="modal-overlay">
    <div class="modal-content">
        <h3 id="modalTitle" style="margin: 0 0 20px 0; font-size: 18px; color: var(--dark-blue);">Thêm Nguyện vọng</h3>
        <form id="wishForm" action="${pageContext.request.contextPath}/thisinh/wishes/save" method="POST">
            
            <input type="hidden" name="idnv" id="wishId" value="">
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 13px; margin-bottom: 6px; font-weight: 500;">Chọn Ngành Xét Tuyển</label>
                <select name="manganh" id="manganhSelect" class="select2-enable" style="width: 100%;" required>
                    <option value="">-- Gõ để tìm kiếm ngành --</option>
                    <c:forEach var="n" items="${listNganh}">
                        <option value="${n.manganh}">${n.manganh} - ${n.tennganh}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="alert-box" style="font-size: 13px; background: #eff6ff; padding: 12px; border-radius: 6px; border: 1px dashed #bfdbfe; color: #1e40af; line-height: 1.5;">
                💡 <strong>Hệ thống thông minh:</strong> Sau khi chọn Ngành, máy chủ sẽ quét các môn thi của bạn, đối chiếu với các tổ hợp môn được phép và tự động chọn ra Tổ hợp mang lại <strong>Điểm Xét Tuyển cao nhất</strong>!
            </div>

            <div style="display: flex; gap: 12px; justify-content: flex-end; margin-top: 24px;">
                <button type="button" class="btn btn-outline" onclick="document.getElementById('wishModal').style.display='none'">Hủy bỏ</button>
                <button type="submit" class="btn btn-primary" style="background: #16a34a;">Lưu nguyện vọng</button>
            </div>
        </form>
    </div>
</div>

<script>
    // init select2
    $(document).ready(function() {
        $('.select2-enable').select2({
            dropdownParent: $('#wishModal'),
            placeholder: "-- Gõ mã ngành hoặc tên ngành để tìm --",
            allowClear: true
        });
    });

    // open modal function
    function openWishModal(action, id, manganh) {
        document.getElementById('modalTitle').innerText = action === 'add' ? 'Thêm Nguyện vọng mới' : 'Chỉnh sửa Nguyện vọng';
        document.getElementById('wishId').value = action === 'add' ? '' : id;
        
        // Set Select2
        if (action === 'edit' && manganh) {
            $('#manganhSelect').val(manganh).trigger('change');
        } else {
            $('#manganhSelect').val('').trigger('change');
        }
        
        document.getElementById('wishModal').style.display = 'flex';
    }

    // init SortableJS
    const container = document.getElementById('wishes-container');
    if (container) {
        new Sortable(container, {
            handle: '.drag-handle', // only allow dragging by the handle
            animation: 150,
            ghostClass: 'sortable-ghost',
            onEnd: function () {
                // get new order of wish IDs after drag-and-drop
                const cards = container.querySelectorAll('.wish-card');
                let ids = [];
                cards.forEach(c => ids.push(c.getAttribute('data-id')));
                
                // post new order to server
                fetch('${pageContext.request.contextPath}/thisinh/wishes/reorder', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'ids=' + ids.join(',')
                }).then(res => {
                    if(res.ok) window.location.reload();
                    else alert("Có lỗi xảy ra khi đổi thứ tự. Vui lòng thử lại!");
                });
            }
        });
    }
</script>

</body>
</html>