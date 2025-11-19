const tasksList = document.getElementById("tasks-list");
const messageBox = document.getElementById("message");
const logoutBtn = document.getElementById("logout-btn");

const accessToken = localStorage.getItem("accessToken");
if (!accessToken) {
    window.location.href = "/auth.html"; // перенаправляем на вход
}

async function fetchTasks() {
    try {
        const response = await fetch("/task/all", {
            headers: { "Authorization": "Bearer " + accessToken }
        });
        if (!response.ok) throw new Error("Не удалось загрузить задачи");
        const tasks = await response.json();
        renderTasks(tasks);
    } catch (err) {
        messageBox.textContent = err.message;
    }
}

function renderTasks(tasks) {
    tasksList.innerHTML = "";
    if (tasks.length === 0) {
        tasksList.innerHTML = "<p>У вас пока нет задач.</p>";
        return;
    }

    tasks.forEach(task => {
        const taskEl = document.createElement("div");
        taskEl.className = "task-card";
        taskEl.innerHTML = `
            <div class="task-title">${task.title}</div>
            <div class="task-desc">${task.description || ""}</div>
            <div class="task-meta">
                <span>${task.status}</span>
                <div>
                    <button onclick="markInProgress('${task.id}')">В работе</button>
                    <button onclick="markDone('${task.id}')">Выполнено</button>
                </div>
            </div>
        `;
        tasksList.appendChild(taskEl);
    });
}

async function markInProgress(id) {
    try {
        await fetch(`/task/in_progress/${id}`, {
            method: "POST",
            headers: { "Authorization": "Bearer " + accessToken }
        });
        fetchTasks();
    } catch (err) {
        messageBox.textContent = err.message;
    }
}

async function markDone(id) {
    try {
        await fetch(`/task/done/${id}`, {
            method: "POST",
            headers: { "Authorization": "Bearer " + accessToken }
        });
        fetchTasks();
    } catch (err) {
        messageBox.textContent = err.message;
    }
}

logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/auth.html";
});

// Загружаем задачи при открытии страницы
fetchTasks();