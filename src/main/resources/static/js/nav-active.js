(function () {
    // 중복 active 제거 후, 가장 일치도 높은 메뉴에 active 부여
    var navLinks = document.querySelectorAll('.navbar .nav-link[href]');
    if (!navLinks.length) return;

    var path = (window.location.pathname || '/').replace(/\/+$/, '') || '/';
    var best = null, bestScore = -1;

    navLinks.forEach(function (link) {
        try {
            var href = new URL(link.href, window.location.origin).pathname.replace(/\/+$/, '') || '/';
            var score = 0;
            if (href === path) score = 1000;                 // 완전 일치 최우선
            else if (path.startsWith(href) && href !== '/')  // prefix 일치 시 경로 길이로 가중치
                score = href.length;

            if (score > bestScore) { bestScore = score; best = link; }
        } catch (_) {}
    });

    if (best) {
        document.querySelectorAll('.navbar .nav-link.active')
            .forEach(function (a) { a.classList.remove('active'); });
        best.classList.add('active');
        best.setAttribute('aria-current', 'page');
    }
})();

    document.addEventListener('DOMContentLoaded', function () {
        // 상위 "근무시간 관리"는 링크 동작 막기 (호버만 사용)
        const workTimeToggle = document.getElementById('workTimeMenu');
        if (workTimeToggle) {
        workTimeToggle.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        });
    }

    // 현재 경로와 동일한 메뉴에 active 부여 (하위가 매칭되면 상위도 active)
    const currentPath = location.pathname.replace(/\/+$/, '');
    const links = document.querySelectorAll('.navbar a.nav-link, .navbar .dropdown-item');

    links.forEach(a => {
    const href = a.getAttribute('href');
    if (!href || href === '#') return;
    const normalized = href.replace(/\/+$/, '');
    if (normalized === currentPath) {
    a.classList.add('active');

    const parentDropdown = a.closest('.dropdown');
    if (parentDropdown) {
        const toggle = parentDropdown.querySelector('.dropdown-toggle');
        if (toggle) toggle.classList.add('active');
        }
    }
    });
});

