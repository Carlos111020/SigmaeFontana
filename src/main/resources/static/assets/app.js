const API_BASE = "/api/v1";
const PORTERIA_LOGIN = {
    correo: "porteria@sigmae.edu.co",
    password: "Porteria123*"
};

let token = localStorage.getItem("sigmaeToken") || "";
let testCards = [];

const sessionStatus = document.querySelector("#sessionStatus");
const loginButton = document.querySelector("#loginButton");
const cardList = document.querySelector("#cardList");
const cardCode = document.querySelector("#cardCode");
const gateForm = document.querySelector("#gateForm");
const scanButton = document.querySelector("#scanButton");
const resultBox = document.querySelector("#resultBox");
const lastEventTime = document.querySelector("#lastEventTime");
const gateArm = document.querySelector("#gateArm");
const studentMarker = document.querySelector("#studentMarker");

async function init() {
    await loadTestCards();
    refreshSession();
    loginButton.addEventListener("click", login);
    gateForm.addEventListener("submit", registerAccess);
}

async function loadTestCards() {
    try {
        const response = await fetch("/assets/test-cards.json");
        testCards = await parseResponse(response);
        renderCards();
    } catch (error) {
        testCards = [];
        cardList.innerHTML = `<p class="hint">No fue posible cargar los carnets de prueba.</p>`;
    }
}

function renderCards() {
    cardList.innerHTML = testCards.map(card => `
        <button class="test-card" type="button" data-code="${card.code}">
            <strong>${card.code}</strong>
            <span>${card.name}</span>
        </button>
    `).join("");

    cardList.querySelectorAll("button").forEach(button => {
        button.addEventListener("click", () => {
            cardCode.value = button.dataset.code;
            cardCode.focus();
        });
    });
}

function refreshSession() {
    if (token) {
        sessionStatus.textContent = "Porteria conectada";
        sessionStatus.className = "status-pill status-ok";
        loginButton.textContent = "Reconectar";
        return;
    }

    sessionStatus.textContent = "Sin conexion";
    sessionStatus.className = "status-pill status-muted";
    loginButton.textContent = "Conectar porteria";
}

async function login() {
    setBusy(true);
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(PORTERIA_LOGIN)
        });
        const data = await parseResponse(response);
        token = data.token;
        localStorage.setItem("sigmaeToken", token);
        refreshSession();
        renderInfo("Sesion iniciada", "La porteria quedo conectada con JWT para registrar ingresos.");
    } catch (error) {
        token = "";
        localStorage.removeItem("sigmaeToken");
        refreshSession();
        renderError(error.message);
    } finally {
        setBusy(false);
    }
}

async function registerAccess(event) {
    event.preventDefault();
    const code = cardCode.value.trim();

    if (!code) {
        renderError("Digita o selecciona un codigo de carnet.");
        return;
    }

    if (!token) {
        await login();
        if (!token) {
            return;
        }
    }

    setBusy(true);
    resetGate();

    try {
        const response = await fetch(`${API_BASE}/talanquera/ingresos`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                codigoTarjeta: code,
                observacion: "Ingreso registrado desde simulador web"
            })
        });
        const data = await parseResponse(response);
        openGate(data.codigoTarjeta);
        renderSuccess(data);
    } catch (error) {
        if (error.status === 401 || error.status === 403) {
            token = "";
            localStorage.removeItem("sigmaeToken");
            refreshSession();
        }
        denyGate(code);
        renderError(error.message);
    } finally {
        setBusy(false);
    }
}

async function parseResponse(response) {
    const text = await response.text();
    const data = text ? JSON.parse(text) : {};

    if (!response.ok) {
        const error = new Error(data.message || data.detail || "No fue posible completar la operacion.");
        error.status = response.status;
        throw error;
    }

    return data;
}

function renderSuccess(data) {
    lastEventTime.textContent = formatDateTime(data.fechaHora);
    const guardians = data.acudientesNotificados.map(acudiente => `
        <div class="guardian">
            <strong>${acudiente.nombres} ${acudiente.apellidos}</strong>
            <div>${acudiente.parentesco} - ${acudiente.correo}</div>
        </div>
    `).join("");

    resultBox.className = "result-card success";
    resultBox.innerHTML = `
        <div class="result-title">Ingreso autorizado</div>
        <div class="data-list">
            <div class="data-row"><span>Estudiante</span><strong>${data.estudiante}</strong></div>
            <div class="data-row"><span>Carnet</span><strong>${data.codigoTarjeta}</strong></div>
            <div class="data-row"><span>Grado</span><strong>${data.grado}</strong></div>
            <div class="data-row"><span>Punto</span><strong>${data.puntoAcceso}</strong></div>
            <div class="data-row"><span>Correo</span><strong>${data.mensajeCorreo}</strong></div>
        </div>
        <div class="guardian-list">${guardians}</div>
    `;
}

function renderInfo(title, message) {
    resultBox.className = "result-card";
    resultBox.innerHTML = `
        <div class="result-title">${title}</div>
        <p>${message}</p>
    `;
}

function renderError(message) {
    lastEventTime.textContent = new Date().toLocaleTimeString("es-CO", { hour: "2-digit", minute: "2-digit" });
    resultBox.className = "result-card error";
    resultBox.innerHTML = `
        <div class="result-title">Acceso no registrado</div>
        <p>${message}</p>
    `;
}

function openGate(code) {
    studentMarker.textContent = code;
    studentMarker.classList.remove("denied");
    gateArm.classList.add("open");
    window.setTimeout(() => gateArm.classList.remove("open"), 1800);
}

function denyGate(code) {
    studentMarker.textContent = code || "ID";
    studentMarker.classList.add("denied");
    gateArm.classList.remove("open");
}

function resetGate() {
    studentMarker.classList.remove("denied");
    gateArm.classList.remove("open");
}

function setBusy(isBusy) {
    scanButton.disabled = isBusy;
    loginButton.disabled = isBusy;
    scanButton.textContent = isBusy ? "Procesando..." : "Registrar ingreso";
}

function formatDateTime(value) {
    return new Intl.DateTimeFormat("es-CO", {
        dateStyle: "short",
        timeStyle: "short"
    }).format(new Date(value));
}

init();
