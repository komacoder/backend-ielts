document.addEventListener("DOMContentLoaded", function () {
    const toggleBtn = document.createElement("button");
    toggleBtn.id = "theme-toggle";
    
    // Check localStorage
    const currentTheme = localStorage.getItem("swagger-theme") || "dark";
    
    function applyTheme(theme) {
        if (theme === "dark") {
            document.body.classList.add("theme-dark");
            toggleBtn.innerHTML = "☀️ Light";
        } else {
            document.body.classList.remove("theme-dark");
            toggleBtn.innerHTML = "🌙 Dark";
        }
        localStorage.setItem("swagger-theme", theme);
    }
    
    applyTheme(currentTheme);
    
    toggleBtn.addEventListener("click", () => {
        const isDark = document.body.classList.contains("theme-dark");
        applyTheme(isDark ? "light" : "dark");
    });
    
    document.body.appendChild(toggleBtn);
});
