const formTitle = document.getElementById("form-title");
const switchText = document.getElementById("switch-text");
const submitBtn = document.getElementById("submit-btn");
const messageBox = document.getElementById("message");

let isLogin = true;
let isRecovery = false;

switchText.addEventListener("click", () => {
    if (!isRecovery) {
        isLogin = !isLogin;
        formTitle.textContent = isLogin ? "Вход" : "Регистрация";
        submitBtn.textContent = isLogin ? "Войти" : "Зарегистрироваться";
        switchText.textContent = isLogin ? "У меня нет аккаунта" : "У меня уже есть аккаунт";
        messageBox.textContent = "";
    }
});

const recoveryLink = document.createElement("span");
recoveryLink.textContent = "Забыли пароль?";
recoveryLink.style.display = "block";
recoveryLink.style.marginTop = "10px";
recoveryLink.style.cursor = "pointer";
recoveryLink.style.color = "#ff0";
recoveryLink.addEventListener("click", () => {
    isRecovery = true;
    formTitle.textContent = "Восстановление пароля";
    submitBtn.textContent = "Сменить пароль";
    switchText.style.display = "none";
    messageBox.textContent = "";
    document.getElementById("password").placeholder = "Новый пароль";
});
document.getElementById("auth-container").appendChild(recoveryLink);

async function sendRequest(url, data) {
    try {
        const response = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || "Ошибка сервера");
        }

        return await response.json();
    } catch (err) {
        throw new Error(err.message);
    }
}

submitBtn.addEventListener("click", async (e) => {
    e.preventDefault();
    messageBox.textContent = "";
    messageBox.classList.remove("success");

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!email || !password) {
        messageBox.textContent = "Заполните все поля";
        return;
    }

    try {
        if (isRecovery) {
            const res = await sendRequest("/api/auth/recovery", { email, password });
            messageBox.textContent = res.message || "Пароль успешно обновлён!";
            messageBox.classList.add("success");
            setTimeout(() => {
                isRecovery = false;
                formTitle.textContent = "Вход";
                submitBtn.textContent = "Войти";
                switchText.style.display = "inline";
                document.getElementById("password").placeholder = "Пароль";
            }, 1500);
        } else if (isLogin) {
            const res = await sendRequest("/api/auth/login", { email, password });
            localStorage.setItem("accessToken", res.accessToken);
            localStorage.setItem("refreshToken", res.refreshToken);
            messageBox.textContent = "Вы успешно вошли!";
            messageBox.classList.add("success");
            setTimeout(() => window.location.href = "/", 1000);
        } else {
            const res = await sendRequest("/api/auth/register", { email, password });
            messageBox.textContent = "Регистрация успешна! Теперь войдите.";
            messageBox.classList.add("success");
            isLogin = true;
            formTitle.textContent = "Вход";
            submitBtn.textContent = "Войти";
            switchText.textContent = "У меня нет аккаунта";
        }
    } catch (err) {
        messageBox.textContent = err.message;
    }
});