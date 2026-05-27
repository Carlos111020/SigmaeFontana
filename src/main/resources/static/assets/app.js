const API_BASE = "/api/v1";
const ROLE_LOGINS = {
    ADMINISTRADOR: {
        label: "Administrador",
        correo: "admin@sigmae.edu.co",
        password: "Admin123*",
        scope: "Gestiona usuarios, catalogos y configuracion general."
    },
    COORDINADOR: {
        label: "Coordinador",
        correo: "coordinador@sigmae.edu.co",
        password: "Coord123*",
        scope: "Consulta estudiantes, dashboard, novedades e historial."
    },
    PORTERIA: {
        label: "Porteria",
        correo: "porteria@sigmae.edu.co",
        password: "Porteria123*",
        scope: "Registra ingresos y salidas desde la talanquera."
    },
    ACUDIENTE: {
        label: "Acudiente",
        correo: "acudiente@sigmae.edu.co",
        password: "Acudiente123*",
        scope: "Consulta sus estudiantes y notificaciones."
    }
};

let token = localStorage.getItem("sigmaeToken") || "";
let activeUser = readStoredUser();
let testCards = [];

const sessionStatus = document.querySelector("#sessionStatus");
const loginButton = document.querySelector("#loginButton");
const roleSelect = document.querySelector("#roleSelect");
const profilePanel = document.querySelector("#profilePanel");
const cardList = document.querySelector("#cardList");
const cardCode = document.querySelector("#cardCode");
const gateForm = document.querySelector("#gateForm");
const scanButton = document.querySelector("#scanButton");
const resultBox = document.querySelector("#resultBox");
const lastEventTime = document.querySelector("#lastEventTime");
const gateArm = document.querySelector("#gateArm");
const studentMarker = document.querySelector("#studentMarker");
const toast = document.querySelector("#toast");
const dashboardPanel = document.querySelector("#dashboardPanel");
const dashboardContent = document.querySelector("#dashboardContent");
const refreshDashboardButton = document.querySelector("#refreshDashboardButton");

async function init() {
    await loadTestCards();
    if (activeUser?.rol && ROLE_LOGINS[activeUser.rol]) {
        roleSelect.value = activeUser.rol;
    }
    refreshSession();
    loginButton.addEventListener("click", login);
    roleSelect.addEventListener("change", () => {
        if (!token) {
            renderProfile();
        }
    });
    gateForm.addEventListener("submit", registerAccess);
    refreshDashboardButton.addEventListener("click", loadDashboard);
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
            <span>${card.name} - ${card.grade}</span>
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
    if (token && activeUser) {
        sessionStatus.textContent = `${roleLabel(activeUser.rol)} conectado`;
        sessionStatus.className = "status-pill status-ok";
        loginButton.textContent = "Cambiar sesion";
        renderProfile();
        updateRoleView();
        return;
    }

    sessionStatus.textContent = "Sin conexion";
    sessionStatus.className = "status-pill status-muted";
    loginButton.textContent = "Iniciar sesion";
    renderProfile();
    updateRoleView();
}

async function login() {
    const credentials = ROLE_LOGINS[roleSelect.value];
    setBusy(true);
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                correo: credentials.correo,
                password: credentials.password
            })
        });
        const data = await parseResponse(response);
        token = data.token;
        activeUser = data.usuario;
        localStorage.setItem("sigmaeToken", token);
        localStorage.setItem("sigmaeUser", JSON.stringify(activeUser));
        refreshSession();
        renderInfo("Sesion iniciada", `Perfil activo: ${roleLabel(activeUser.rol)}.`);
        if (activeUser.rol === "COORDINADOR") {
            await loadDashboard();
        }
    } catch (error) {
        clearSession();
        renderError(error.message);
    } finally {
        setBusy(false);
    }
}

async function registerAccess(event) {
    event.preventDefault();
    const code = cardCode.value.trim().toUpperCase();

    if (!code) {
        renderError("Digita o selecciona un codigo de carnet.");
        return;
    }

    if (!token) {
        roleSelect.value = "PORTERIA";
        await login();
        if (!token) {
            return;
        }
    }

    if (!canUseGate()) {
        denyGate(code);
        renderError("Este perfil no puede registrar ingresos. Selecciona Porteria o Coordinador.");
        return;
    }

    setBusy(true);
    resetGate();
    const mode = getAccessMode();

    try {
        const endpoint = mode === "ingresos" ? `${API_BASE}/talanquera/ingresos` : `${API_BASE}/registros-acceso/salidas`;
        const payload = mode === "ingresos"
                ? { codigoTarjeta: code, observacion: "Ingreso registrado desde simulador web" }
                : {
                    identificadorEstudiante: code,
                    puntoAccesoId: 1,
                    observacion: "Salida registrada desde simulador web",
                    crearNovedadSalidaAnticipada: false
                };
        const response = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(payload)
        });
        const data = await parseResponse(response);
        const normalized = normalizeAccessResponse(data, code, mode);
        openGate(normalized.codigoTarjeta);
        renderSuccess(normalized, false);
        notifyGuardian(normalized, false);
    } catch (error) {
        if (error.status === 401 || error.status === 403) {
            clearSession();
            denyGate(code);
            renderError("La sesion expiro o el rol no tiene permisos.");
            return;
        }

        const demoData = buildDemoAccess(code, mode);
        if (demoData) {
            openGate(code);
            renderSuccess(demoData, true);
            notifyGuardian(demoData, true);
            return;
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

function buildDemoAccess(code, mode = "ingresos") {
    const card = testCards.find(item => item.code === code);
    if (!card) {
        return null;
    }

    const now = new Date().toISOString();
    const [guardianNames, guardianLastName] = splitName(card.guardian.name);
    return {
        registroId: null,
        fechaHora: now,
        codigoTarjeta: card.code,
        estudianteId: null,
        estudiante: card.name,
        grado: card.grade,
        estadoPermanencia: mode === "ingresos" ? "DENTRO_DEL_PLANTEL" : "FUERA_DEL_PLANTEL",
        puntoAcceso: "Porteria principal",
        mensajeCorreo: `Correo demo enviado a ${card.guardian.email} con hora de ${mode === "ingresos" ? "llegada" : "salida"}: ${formatDateTime(now)}`,
        operacion: mode === "ingresos" ? "INGRESO" : "SALIDA",
        acudientesNotificados: [
            {
                id: null,
                nombres: guardianNames,
                apellidos: guardianLastName,
                parentesco: card.guardian.relationship,
                correo: card.guardian.email,
                correoSimuladoEnviado: true
            }
        ]
    };
}

function normalizeAccessResponse(data, code, mode) {
    if (data.codigoTarjeta) {
        return {
            ...data,
            operacion: "INGRESO"
        };
    }

    const card = testCards.find(item => item.code === code);
    const [guardianNames, guardianLastName] = splitName(card?.guardian?.name || "Acudiente");
    return {
        registroId: data.id,
        fechaHora: data.fechaHora,
        codigoTarjeta: code,
        estudianteId: data.estudianteId,
        estudiante: data.estudiante,
        grado: card?.grade || "No registrado",
        estadoPermanencia: data.estadoPermanencia,
        puntoAcceso: data.puntoAcceso,
        mensajeCorreo: `Correo simulado enviado al acudiente con hora de salida: ${formatDateTime(data.fechaHora)}`,
        operacion: mode === "ingresos" ? "INGRESO" : "SALIDA",
        acudientesNotificados: [
            {
                id: null,
                nombres: guardianNames,
                apellidos: guardianLastName,
                parentesco: card?.guardian?.relationship || "Acudiente",
                correo: card?.guardian?.email || "correo.acudiente@example.com",
                correoSimuladoEnviado: true
            }
        ]
    };
}

function renderSuccess(data, isDemo) {
    const displayData = applyCardOverrides(data);
    lastEventTime.textContent = formatDateTime(displayData.fechaHora);
    const guardians = displayData.acudientesNotificados.map(acudiente => `
        <div class="guardian">
            <strong>${acudiente.nombres} ${acudiente.apellidos}</strong>
            <div>${acudiente.parentesco} - ${acudiente.correo}</div>
        </div>
    `).join("");

    resultBox.className = "result-card success";
    resultBox.innerHTML = `
        <div class="result-title">${displayData.operacion === "SALIDA" ? "Salida registrada" : "Ingreso autorizado"}</div>
        ${isDemo ? `<p class="demo-note">Registro demo desde archivo de prueba. El backend queda intacto si el estudiante no existe en BD.</p>` : ""}
        <div class="data-list">
            <div class="data-row"><span>Estudiante</span><strong>${displayData.estudiante}</strong></div>
            <div class="data-row"><span>Carnet</span><strong>${displayData.codigoTarjeta}</strong></div>
            <div class="data-row"><span>Grado</span><strong>${displayData.grado}</strong></div>
            <div class="data-row"><span>Punto</span><strong>${displayData.puntoAcceso}</strong></div>
            <div class="data-row"><span>Correo</span><strong>${displayData.mensajeCorreo}</strong></div>
        </div>
        <div class="guardian-list">${guardians}</div>
    `;
}

function applyCardOverrides(data) {
    const card = testCards.find(item => item.code === data.codigoTarjeta);
    if (!card) {
        return data;
    }

    return {
        ...data,
        grado: card.grade
    };
}

function notifyGuardian(data, isDemo) {
    const displayData = applyCardOverrides(data);
    const guardian = displayData.acudientesNotificados[0];
    const recipient = guardian ? guardian.correo : "acudiente";
    const mode = isDemo ? "Correo demo enviado" : "Correo enviado";
    const operation = displayData.operacion === "SALIDA" ? "salida" : "ingreso";
    showToast(`${mode} a ${recipient} por el ${operation} de ${displayData.estudiante}.`);
}

async function loadDashboard() {
    if (!token || activeUser?.rol !== "COORDINADOR") {
        dashboardContent.innerHTML = `<p class="hint">Inicia sesion como Coordinador para consultar metricas reales.</p>`;
        return;
    }

    dashboardContent.innerHTML = `<p class="hint">Cargando dashboard...</p>`;
    try {
        const [metrics, registros, novedades] = await Promise.all([
            apiGet("/dashboard/metricas"),
            apiGet("/registros-acceso?size=5"),
            apiGet("/novedades")
        ]);
        renderDashboard(metrics, registros.content || [], novedades);
    } catch (error) {
        dashboardContent.innerHTML = `<p class="hint">${error.message}</p>`;
    }
}

async function apiGet(path) {
    const response = await fetch(`${API_BASE}${path}`, {
        headers: { "Authorization": `Bearer ${token}` }
    });
    return parseResponse(response);
}

function renderDashboard(metrics, registros, novedades) {
    dashboardContent.innerHTML = `
        <div class="metric-grid">
            <div><span>Presentes</span><strong>${metrics.estudiantesPresentes}</strong></div>
            <div><span>Ingresos hoy</span><strong>${metrics.ingresosDelDia}</strong></div>
            <div><span>Salidas hoy</span><strong>${metrics.salidasDelDia}</strong></div>
            <div><span>Novedades</span><strong>${metrics.novedadesPendientes}</strong></div>
        </div>
        <div class="dashboard-list">
            <h3>Ultimos accesos</h3>
            ${registros.map(item => `<p>${item.tipoRegistro} - ${item.estudiante} - ${formatDateTime(item.fechaHora)}</p>`).join("") || "<p>Sin registros.</p>"}
        </div>
        <div class="dashboard-list">
            <h3>Novedades</h3>
            ${novedades.slice(0, 5).map(item => `<p>${item.estado} - ${item.estudiante} - ${item.tipoNovedad}</p>`).join("") || "<p>Sin novedades.</p>"}
        </div>
    `;
}

function updateRoleView() {
    const isCoordinator = activeUser?.rol === "COORDINADOR";
    dashboardPanel.classList.toggle("visible", isCoordinator);
    if (!isCoordinator) {
        dashboardContent.innerHTML = "";
    }
}

function showToast(message) {
    toast.textContent = message;
    toast.classList.add("visible");
    window.clearTimeout(showToast.timeoutId);
    showToast.timeoutId = window.setTimeout(() => {
        toast.classList.remove("visible");
    }, 4200);
}

function renderProfile() {
    const selected = ROLE_LOGINS[roleSelect.value];
    if (!activeUser || !token) {
        profilePanel.innerHTML = `
            <div>
                <strong>Perfil no iniciado</strong>
                <span>Selecciona un rol: ${selected.label}</span>
            </div>
            <p>${selected.scope}</p>
        `;
        return;
    }

    profilePanel.innerHTML = `
        <div>
            <strong>${activeUser.nombres} ${activeUser.apellidos}</strong>
            <span>${roleLabel(activeUser.rol)} - ${activeUser.correo}</span>
        </div>
        <p>${ROLE_LOGINS[activeUser.rol]?.scope || "Perfil activo en SIGMAE."}</p>
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
    scanButton.textContent = isBusy ? "Procesando..." : "Registrar";
}

function canUseGate() {
    return activeUser?.rol === "PORTERIA" || activeUser?.rol === "COORDINADOR";
}

function clearSession() {
    token = "";
    activeUser = null;
    localStorage.removeItem("sigmaeToken");
    localStorage.removeItem("sigmaeUser");
    refreshSession();
}

function readStoredUser() {
    try {
        return JSON.parse(localStorage.getItem("sigmaeUser"));
    } catch (error) {
        return null;
    }
}

function roleLabel(role) {
    return ROLE_LOGINS[role]?.label || role || "Usuario";
}

function splitName(fullName) {
    const parts = fullName.split(" ");
    if (parts.length === 1) {
        return [fullName, ""];
    }
    return [parts.slice(0, -1).join(" "), parts.at(-1)];
}

function formatDateTime(value) {
    return new Intl.DateTimeFormat("es-CO", {
        dateStyle: "short",
        timeStyle: "short"
    }).format(new Date(value));
}

function getAccessMode() {
    return document.querySelector("input[name='accessMode']:checked")?.value || "ingresos";
}

init();
