// Unified sidebar toggle functionality - works for both mobile and desktop
function toggleSidebar() {
    try {
        const sidebar = document.getElementById('sidebar');
        if (!sidebar) {
            console.error('Sidebar element not found');
            return false;
        }
        
        const isDesktop = window.innerWidth > 768;
        
        if (isDesktop) {
            // Desktop: toggle collapsed state
            sidebar.classList.toggle('collapsed');
            const isCollapsed = sidebar.classList.contains('collapsed');
            localStorage.setItem('sidebarCollapsed', isCollapsed ? 'true' : 'false');
        } else {
            // Mobile: toggle mobile-open
            sidebar.classList.toggle('mobile-open');
        }
        return false; // Prevent default behavior
    } catch (error) {
        console.error('Error toggling sidebar:', error);
        return false;
    }
}

// Mobile sidebar toggle functionality (alias for backward compatibility)
function toggleMobileSidebar() {
    return toggleSidebar();
}

// Toggle Employee Management submenu
function toggleEmployeeSubmenu(event) {
    if (event) event.preventDefault();
    const sidebar = document.getElementById('sidebar');
    if (!sidebar) return;
    const parent = document.querySelector('.sidebar-menu-parent');
    if (!parent) return;
    parent.classList.toggle('expanded');
}

// Make functions globally available
window.toggleSidebar = toggleSidebar;
window.toggleMobileSidebar = toggleMobileSidebar;
window.toggleEmployeeSubmenu = toggleEmployeeSubmenu;

// Initialize sidebar on page load
document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.getElementById('sidebar');
    if (!sidebar) return;
    
    // Load sidebar state from localStorage (desktop only)
    if (window.innerWidth > 768) {
        const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
        if (isCollapsed) {
            sidebar.classList.add('collapsed');
        }
    }
    
    // Close mobile sidebar when clicking outside
    document.addEventListener('click', function(event) {
        if (window.innerWidth <= 768 && sidebar) {
            const mobileToggle = document.querySelector('.mobile-menu-toggle');
            const expandToggle = document.querySelector('.sidebar-expand-toggle');
            const isClickInsideSidebar = sidebar.contains(event.target);
            const isClickOnToggle = (mobileToggle && mobileToggle.contains(event.target)) || 
                                   (expandToggle && expandToggle.contains(event.target));
            
            if (!isClickInsideSidebar && !isClickOnToggle && sidebar.classList.contains('mobile-open')) {
                sidebar.classList.remove('mobile-open');
            }
        }
    });
    
    // Handle window resize
    window.addEventListener('resize', function() {
        if (!sidebar) return;
        
        if (window.innerWidth > 768) {
            // Desktop: remove mobile-open, restore collapsed state
            sidebar.classList.remove('mobile-open');
            const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
            if (isCollapsed) {
                sidebar.classList.add('collapsed');
            } else {
                sidebar.classList.remove('collapsed');
            }
        } else {
            // Mobile: remove collapsed state
            sidebar.classList.remove('collapsed');
        }
    });
});
