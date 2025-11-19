const tasksList = document.getElementById("tasks-list");
const messageBox = document.getElementById("message");
const logoutBtn = document.getElementById("logout-btn");
const createTaskBtn = document.getElementById("create-task-btn");
const filterTasksBtn = document.getElementById("filter-tasks-btn");
const sortTasksBtn = document.getElementById("sort-tasks-btn");

const activeFilters = document.getElementById("active-filters");
const filtersList = document.getElementById("filters-list");
const clearFiltersBtn = document.getElementById("clear-filters-btn");
const activeSort = document.getElementById("active-sort");
const sortInfo = document.getElementById("sort-info");
const clearSortBtn = document.getElementById("clear-sort-btn");

const createTaskModal = document.getElementById("create-task-modal");
const editTaskModal = document.getElementById("edit-task-modal");
const filterModal = document.getElementById("filter-modal");
const sortModal = document.getElementById("sort-modal");

const addTaskForm = document.getElementById("add-task-form");
const editTaskForm = document.getElementById("edit-task-form");
const filterForm = document.getElementById("filter-form");
const sortForm = document.getElementById("sort-form");

const closeButtons = document.querySelectorAll('.close');
const cancelCreateBtn = document.getElementById("cancel-create-btn");
const cancelEditBtn = document.getElementById("cancel-edit-btn");
const cancelFilterBtn = document.getElementById("cancel-filter-btn");
const cancelSortBtn = document.getElementById("cancel-sort-btn");
const resetFilterBtn = document.getElementById("reset-filter-btn");
const resetSortBtn = document.getElementById("reset-sort-btn");

const accessToken = localStorage.getItem("accessToken");
if (!accessToken) {
    window.location.href = "/auth.html";
}

let currentTasks = [];
let currentFilters = {
    keyword: '',
    category: '',
    priority: '',
    status: '',
    overdue: false
};
let currentSort = {
    sortBy: 'createdAt',
    ascending: false
};

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

function getStatusText(status) {
    const statuses = {
        'CREATED': 'Создана',
        'TODO': 'К выполнению',
        'IN_PROGRESS': 'В работе',
        'DONE': 'Выполнено'
    };
    return statuses[status] || 'Создана';
}

function getSortFieldText(field) {
    const fields = {
        'createdAt': 'Дата создания',
        'dueDate': 'Срок выполнения',
        'title': 'Название',
        'priority': 'Приоритет'
    };
    return fields[field] || field;
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
        currentTasks = tasks;
        applyFiltersAndSort();
    } catch (err) {
        showMessage(err.message, true);
    }
}

async function applyFiltersAndSort() {
    const params = new URLSearchParams();

    if (currentFilters.keyword) params.append('keyword', currentFilters.keyword);
    if (currentFilters.category) params.append('category', currentFilters.category);
    if (currentFilters.priority) params.append('priority', currentFilters.priority);
    if (currentFilters.status) params.append('status', currentFilters.status);
    if (currentFilters.overdue) params.append('overdue', 'true');

    try {
        let tasksToDisplay;

        if (params.toString()) {
            const response = await fetch(`/task/filter?${params.toString()}`, {
                headers: {
                    "Authorization": "Bearer " + accessToken,
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) throw new Error("Ошибка при фильтрации задач");
            tasksToDisplay = await response.json();
        } else {
            tasksToDisplay = currentTasks;
        }

        if (tasksToDisplay.length > 0) {
            tasksToDisplay = await applySorting(tasksToDisplay);
        }

        renderTasks(tasksToDisplay);
        updateActiveFiltersDisplay();
        updateActiveSortDisplay();

    } catch (err) {
        showMessage(err.message, true);
        renderTasks(currentTasks);
    }
}

async function applySorting(tasks) {
    if (tasks.length === 0) return tasks;

    const taskIds = tasks.map(task => task.id);
    const params = new URLSearchParams({
        sortBy: currentSort.sortBy,
        ascending: currentSort.ascending
    });

    taskIds.forEach(id => params.append('taskIds', id));

    try {
        const response = await fetch(`/task/sort?${params.toString()}`, {
            headers: {
                "Authorization": "Bearer " + accessToken,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) throw new Error("Ошибка при сортировке задач");
        return await response.json();
    } catch (err) {
        showMessage("Ошибка сортировки: " + err.message, true);
        return tasks;
    }
}

function updateActiveFiltersDisplay() {
    const activeFiltersArray = [];

    if (currentFilters.keyword) {
        activeFiltersArray.push(`Поиск: "${currentFilters.keyword}"`);
    }
    if (currentFilters.category) {
        activeFiltersArray.push(`Категория: ${getCategoryText(currentFilters.category)}`);
    }
    if (currentFilters.priority) {
        activeFiltersArray.push(`Приоритет: ${getPriorityText(currentFilters.priority)}`);
    }
    if (currentFilters.status) {
        activeFiltersArray.push(`Статус: ${getStatusText(currentFilters.status)}`);
    }
    if (currentFilters.overdue) {
        activeFiltersArray.push('Только просроченные');
    }

    if (activeFiltersArray.length > 0) {
        filtersList.innerHTML = activeFiltersArray.map(filter =>
            `<span class="active-filter-tag">${filter}</span>`
        ).join('');
        activeFilters.style.display = 'block';
    } else {
        activeFilters.style.display = 'none';
    }
}

function updateActiveSortDisplay() {
    if (currentSort.sortBy !== 'createdAt' || !currentSort.ascending) {
        const orderText = currentSort.ascending ? 'по возрастанию' : 'по убыванию';
        sortInfo.textContent = `${getSortFieldText(currentSort.sortBy)} (${orderText})`;
        activeSort.style.display = 'block';
    } else {
        activeSort.style.display = 'none';
    }
}

function resetFilters() {
    currentFilters = {
        keyword: '',
        category: '',
        priority: '',
        status: '',
        overdue: false
    };

    document.getElementById('filter-keyword').value = '';
    document.getElementById('filter-category').value = '';
    document.getElementById('filter-priority').value = '';
    document.getElementById('filter-status').value = '';
    document.getElementById('filter-overdue').checked = false;

    applyFiltersAndSort();
    closeFilterModal();
}

function resetSort() {
    currentSort = {
        sortBy: 'createdAt',
        ascending: false
    };

    document.getElementById('sort-by').value = 'createdAt';
    document.getElementById('sort-order').value = 'false';

    applyFiltersAndSort();
    closeSortModal();
}

function renderTasks(tasks) {
    tasksList.innerHTML = "";
    if (tasks.length === 0) {
        tasksList.innerHTML = "<p class='no-tasks'>Задачи не найдены. Попробуйте изменить фильтры.</p>";
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

function openFilterModal() {
    document.getElementById('filter-keyword').value = currentFilters.keyword;
    document.getElementById('filter-category').value = currentFilters.category;
    document.getElementById('filter-priority').value = currentFilters.priority;
    document.getElementById('filter-status').value = currentFilters.status;
    document.getElementById('filter-overdue').checked = currentFilters.overdue;

    filterModal.style.display = 'block';
}

function closeFilterModal() {
    filterModal.style.display = 'none';
}

function openSortModal() {
    document.getElementById('sort-by').value = currentSort.sortBy;
    document.getElementById('sort-order').value = currentSort.ascending.toString();

    sortModal.style.display = 'block';
}

function closeSortModal() {
    sortModal.style.display = 'none';
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
        const task = currentTasks.find(t => t.id === taskId);

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

createTaskBtn.addEventListener("click", openCreateTaskModal);
filterTasksBtn.addEventListener("click", openFilterModal);
sortTasksBtn.addEventListener("click", openSortModal);

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

    saveTask(taskData);
});

filterForm.addEventListener("submit", (e) => {
    e.preventDefault();

    currentFilters = {
        keyword: document.getElementById('filter-keyword').value.trim(),
        category: document.getElementById('filter-category').value,
        priority: document.getElementById('filter-priority').value,
        status: document.getElementById('filter-status').value,
        overdue: document.getElementById('filter-overdue').checked
    };

    applyFiltersAndSort();
    closeFilterModal();
});

sortForm.addEventListener("submit", (e) => {
    e.preventDefault();

    currentSort = {
        sortBy: document.getElementById('sort-by').value,
        ascending: document.getElementById('sort-order').value === 'true'
    };

    applyFiltersAndSort();
    closeSortModal();
});

clearFiltersBtn.addEventListener("click", resetFilters);
clearSortBtn.addEventListener("click", resetSort);
resetFilterBtn.addEventListener("click", resetFilters);
resetSortBtn.addEventListener("click", resetSort);

closeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        closeCreateTaskModal();
        closeEditTaskModal();
        closeFilterModal();
        closeSortModal();
    });
});

cancelCreateBtn.addEventListener('click', closeCreateTaskModal);
cancelEditBtn.addEventListener('click', closeEditTaskModal);
cancelFilterBtn.addEventListener('click', closeFilterModal);
cancelSortBtn.addEventListener('click', closeSortModal);

window.addEventListener('click', (e) => {
    if (e.target === createTaskModal) closeCreateTaskModal();
    if (e.target === editTaskModal) closeEditTaskModal();
    if (e.target === filterModal) closeFilterModal();
    if (e.target === sortModal) closeSortModal();
});

logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/auth.html";
});

fetchTasks();