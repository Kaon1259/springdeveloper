(function () {
    var currentPath = (window.location.pathname || '/').replace(/\/+$/, '') || '/';
    var links = document.querySelectorAll('.main-menu-link[href]');
    var best = null, bestScore = -1;

    links.forEach(function (link) {
        try {
            var href = new URL(link.href, window.location.origin).pathname.replace(/\/+$/, '') || '/';
            var score = 0;
            if (href === currentPath) score = 1000;
            else if (currentPath.startsWith(href) && href !== '/') score = href.length;
            if (score > bestScore) { bestScore = score; best = link; }
        } catch (_) {}
    });

    if (best && bestScore > 0) {
        links.forEach(function (a) { a.classList.remove('active'); });
        best.classList.add('active');
        best.setAttribute('aria-current', 'page');
    }
})();
