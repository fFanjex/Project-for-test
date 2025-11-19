const tasksList = document.getElementById("tasks-list");
const messageBox = document.getElementById("message");
const logoutBtn = document.getElementById("logout-btn");
const createTaskBtn = document.getElementById("create-task-btn");

const createTaskModal = document.getElementById("create-task-modal");
const editTaskModal = document.getElementById("edit-task-modal");
const addTaskForm = document.getElementById("add-task-form");
const editTaskForm = document.getElementById("edit-task-form");

const closeButtons = document.querySelectorAll('.close');
const cancelCreateBtn = document.getElementById("cancel-create-btn");
const cancelEditBtn = document.getElementById("cancel-edit-btn");

const accessToken = localStorage.getItem("accessToken");
if (!accessToken) {
    window.location.href = "/auth.html";
}

function showMessage(message, isError = false) {
    messageBox.textContent = message;
    messageBox.className = isError ? "message error" : "message success";
    setTimeout(() => {
        messageBox.textContent = "";
        messageBox.className = "message";
    }, 3000);
}

function formatToLocalDateTime(dateString) {
    if (!dateString) return null;
    return dateString + 'T18:00:00';
}

function formatFromLocalDateTime(dateTimeString) {
    if (!dateTimeString) return '';
    return dateTimeString.split('T')[0];
}

function getCategoryText(category) {
    const categories = {
        'WORK': 'Работа',
        'PERSONAL': 'Личное',
        'HEALTH': 'Здоровье'
    };
    return categories[category] || category;
}

async function fetchTasks() {
    try {
        const response = await fetch("/task/all", {
            headers: {
                "Authorization": "Bearer " + accessToken,
                "Content-Type": "application/json"
            }
        });

        if (response.status === 401) {
            localStorage.removeItem("accessToken");
            window.location.href = "/auth.html";
            return;
        }

        if (!response.ok) throw new Error("Не удалось загрузить задачи");
        const tasks = await response.json();
        renderTasks(tasks);
    } catch (err) {
        showMessage(err.message, true);
    }
}

function renderTasks(tasks) {
    tasksList.innerHTML = "";
    if (tasks.length === 0) {
        tasksList.innerHTML = "<p class='no-tasks'>У вас пока нет задач. Создайте первую задачу!</p>";
        return;
    }

    tasks.forEach(task => {
        const taskEl = document.createElement("div");
        taskEl.className = `task-card task-${task.status?.toLowerCase() || 'todo'}`;
        taskEl.innerHTML = `
            <div class="task-header">
                <h3 class="task-title">${escapeHtml(task.title)}</h3>
                <div class="task-actions">
                    <button class="btn-edit" onclick="editTask('${task.id}')">✏️</button>
                    <button class="btn-delete" onclick="deleteTask('${task.id}')">🗑️</button>
                </div>
            </div>
            <div class="task-desc">${escapeHtml(task.description || "")}</div>
            <div class="task-details">
                <span class="task-category">🏷️ ${getCategoryText(task.category)}</span>
                <span class="task-priority priority-${task.priority?.toLowerCase() || 'medium'}">${getPriorityText(task.priority)}</span>
                ${task.dueDate ? `<span class="task-due">📅 ${new Date(task.dueDate).toLocaleDateString()}</span>` : ''}
                ${task.overdue ? `<span class="task-overdue">⚠️ Просрочено</span>` : ''}
            </div>
            <div class="task-meta">
                <span class="task-status status-${task.status?.toLowerCase() || 'todo'}">${getStatusText(task.status)}</span>
                <div class="status-buttons">
                    ${task.status !== 'IN_PROGRESS' && task.status !== 'DONE' ?
            `<button class="btn-in-progress" onclick="markInProgress('${task.id}')">В работу</button>` : ''}
                    ${task.status === 'IN_PROGRESS' ?
            `<button class="btn-done" onclick="markDone('${task.id}')">Завершить</button>` : ''}
                </div>
            </div>
        `;
        tasksList.appendChild(taskEl);
    });
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function getPriorityText(priority) {
    const priorities = {
        'LOW': 'Низкий',
        'MEDIUM': 'Средний',
        'HIGH': 'Высокий'
    };
    return priorities[priority] || 'Средний';
}

function getStatusText(status) {
    const statuses = {
        'CREATED': 'Создана',
        'TODO': 'К выполнению',
        'IN_PROGRESS': 'В работе',
        'DONE': 'Выполнено'
    };
    return statuses[status] || 'Создана';
}

async function addTask(taskData) {
    try {
        const response = await fetch("/task/add", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + accessToken,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(taskData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Не удалось добавить задачу");
        }

        showMessage("Задача успешно добавлена!");
        closeCreateTaskModal();
        fetchTasks();
    } catch (err) {
        showMessage(err.message, true);
    }
}

async function editTask(taskId) {
    try {
        const response = await fetch("/task/all", {
            headers: {
                "Authorization": "Bearer " + accessToken,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) throw new Error("Не удалось загрузить задачи");

        const tasks = await response.json();
        const task = tasks.find(t => t.id === taskId);

        if (task) {
            openEditTaskModal(task);
        } else {
            throw new Error("Задача не найдена");
        }
    } catch (err) {
        showMessage(err.message, true);
    }
}

async function saveTask(taskData) {
    try {
        const response = await fetch(`/task/edit/${taskData.id}`, {
            method: "PUT",
            headers: {
                "Authorization": "Bearer " + accessToken,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                title: taskData.title,
                description: taskData.description,
                dueDate: taskData.dueDate,
                priority: taskData.priority,
                category: taskData.category
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Не удалось обновить задачу");
        }

        showMessage("Задача обновлена!");
        closeEditTaskModal();
        fetchTasks();
    } catch (err) {
        showMessage(err.message, true);
    }
}

async function deleteTask(id) {
    if (!confirm("Вы уверены, что хотите удалить эту задачу?")) return;

    try {
        const response = await fetch(`/task/delete/${id}`, {
            method: "DELETE",
            headers: { "Authorization": "Bearer " + accessToken }
        });

        if (!response.ok) throw new Error("Не удалось удалить задачу");

        showMessage("Задача удалена!");
        fetchTasks();
    } catch (err) {
        showMessage(err.message, true);
    }
}

async function markInProgress(id) {
    try {
        const response = await fetch(`/task/in_progress/${id}`, {
            method: "POST",
            headers: { "Authorization": "Bearer " + accessToken }
        });

        if (!response.ok) throw new Error("Не удалось обновить статус");

        showMessage("Статус обновлен на 'В работе'");
        fetchTasks();
    } catch (err) {
        showMessage(err.message, true);
    }
}

async function markDone(id) {
    try {
        const response = await fetch(`/task/done/${id}`, {
            method: "POST",
            headers: { "Authorization": "Bearer " + accessToken }
        });

        if (!response.ok) throw new Error("Не удалось обновить статус");

        showMessage("Задача выполнена!");
        fetchTasks();
    } catch (err) {
        showMessage(err.message, true);
    }
}

function openCreateTaskModal() {
    createTaskModal.style.display = 'block';
    addTaskForm.reset();
}

function closeCreateTaskModal() {
    createTaskModal.style.display = 'none';
}

function openEditTaskModal(task) {
    document.getElementById('edit-task-id').value = task.id;
    document.getElementById('edit-task-title').value = task.title;
    document.getElementById('edit-task-description').value = task.description || '';
    document.getElementById('edit-task-dueDate').value = formatFromLocalDateTime(task.dueDate);
    document.getElementById('edit-task-priority').value = task.priority || 'MEDIUM';
    document.getElementById('edit-task-category').value = task.category || 'PERSONAL';

    editTaskModal.style.display = 'block';
}

function closeEditTaskModal() {
    editTaskModal.style.display = 'none';
}

createTaskBtn.addEventListener("click", openCreateTaskModal);

addTaskForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const taskData = {
        title: document.getElementById("task-title").value,
        description: document.getElementById("task-description").value,
        dueDate: formatToLocalDateTime(document.getElementById("task-dueDate").value),
        priority: document.getElementById("task-priority").value,
        category: document.getElementById("task-category").value,
        status: "CREATED"
    };

    console.log("Отправляемые данные:", taskData);
    addTask(taskData);
});

editTaskForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const taskData = {
        id: document.getElementById("edit-task-id").value,
        title: document.getElementById("edit-task-title").value,
        description: document.getElementById("edit-task-description").value,
        dueDate: formatToLocalDateTime(document.getElementById("edit-task-dueDate").value),
        priority: document.getElementById("edit-task-priority").value,
        category: document.getElementById("edit-task-category").value
    };

    console.log("Обновляемые данные:", taskData);
    saveTask(taskData);
});

closeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        closeCreateTaskModal();
        closeEditTaskModal();
    });
});

cancelCreateBtn.addEventListener('click', closeCreateTaskModal);
cancelEditBtn.addEventListener('click', closeEditTaskModal);

window.addEventListener('click', (e) => {
    if (e.target === createTaskModal) {
        closeCreateTaskModal();
    }
    if (e.target === editTaskModal) {
        closeEditTaskModal();
    }
});

logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/auth.html";
});

fetchTasks();