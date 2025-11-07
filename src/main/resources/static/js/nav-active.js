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
