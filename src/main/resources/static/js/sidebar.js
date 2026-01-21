// Sidebar toggle functionality (expand/collapse)
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        sidebar.classList.toggle('collapsed');
        localStorage.setItem('sidebarCollapsed', sidebar.classList.contains('collapsed'));
    }
}

// Initialize sidebar on page load
document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        // Load sidebar state from localStorage
        if (localStorage.getItem('sidebarCollapsed') === 'true') {
            sidebar.classList.add('collapsed');
        }
        
        // Mobile menu toggle
        const mobileToggle = document.querySelector('.mobile-menu-toggle');
        if (mobileToggle) {
            mobileToggle.addEventListener('click', function(e) {
                e.stopPropagation();
                sidebar.classList.toggle('mobile-open');
            });
        }
        
        // Close mobile sidebar when clicking outside
        document.addEventListener('click', function(event) {
            if (window.innerWidth <= 768 && sidebar) {
                const isClickInsideSidebar = sidebar.contains(event.target);
                const isClickOnToggle = mobileToggle && mobileToggle.contains(event.target);
                
                if (!isClickInsideSidebar && !isClickOnToggle && sidebar.classList.contains('mobile-open')) {
                    sidebar.classList.remove('mobile-open');
                }
            }
        });
        
        // Close mobile sidebar when window is resized to desktop
        window.addEventListener('resize', function() {
            if (window.innerWidth > 768 && sidebar) {
                sidebar.classList.remove('mobile-open');
            }
        });
    }
});
