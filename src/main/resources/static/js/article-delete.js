// 모든 삭제 버튼 선택

const messageContainer = document.getElementById('message-container');

const alertBox = messageContainer?.querySelector('.alert');

if (alertBox && alertBox.textContent.trim() !== '') {
    setTimeout(() => {
        alertBox.classList.add('fade-out');
        setTimeout(() => alertBox.remove(), 500);
    }, 3000);
}

document.querySelectorAll('.btn-delete').forEach(button => {
    button.addEventListener('click', async event => {
        const row = event.target.closest('tr');
        const articleId = row.getAttribute('data-id');

        if (!confirm(`도서 ID ${articleId}를 정말 삭제하시겠습니까?`)) return;

        try {
            // Axios로 삭제 요청 (POST)
            const response = await axios.delete(`/articles/${articleId}/delete`);

            // 테이블에서 해당 행 제거
            row.remove();

            // ✅ 2. 기존 메시지 제거
            messageContainer.innerHTML = '';

            // ✅ 3. 새 메시지 추가
            const alertDiv = document.createElement('div');
            alertDiv.className = 'alert alert-success';
            alertDiv.role = 'alert';
            alertDiv.textContent = `도서(ID: ${articleId})가 삭제되었습니다.`;

            messageContainer.appendChild(alertDiv);

            setTimeout(() => {
                alertDiv.classList.add('fade-out');
                setTimeout(() => alertDiv.remove(), 500);
            }, 3000);
            // 또는 페이지 새로고침
            // window.location.reload();

        } catch (error) {
            console.error(error);
            alert('삭제 중 오류가 발생했습니다.');
        }
    });
});
