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
        scope: "Consulta el estado, grado y hora de llegada de su estudiante."
    }
};

let token = localStorage.getItem("sigmaeToken") || "";
let activeUser = readStoredUser();
let testCards = [];
let adminStudents = [];
let adminGrades = [];

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
const gateWorkspace = document.querySelector("#gateWorkspace");
const guardianPanel = document.querySelector("#guardianPanel");
const guardianContent = document.querySelector("#guardianContent");
const refreshGuardianButton = document.querySelector("#refreshGuardianButton");
const adminPanel = document.querySelector("#adminPanel");
const refreshStudentsButton = document.querySelector("#refreshStudentsButton");
const studentForm = document.querySelector("#studentForm");
const studentFormTitle = document.querySelector("#studentFormTitle");
const studentId = document.querySelector("#studentId");
const studentCode = document.querySelector("#studentCode");
const studentDocument = document.querySelector("#studentDocument");
const studentNames = document.querySelector("#studentNames");
const studentLastNames = document.querySelector("#studentLastNames");
const studentGradeId = document.querySelector("#studentGradeId");
const saveStudentButton = document.querySelector("#saveStudentButton");
const clearStudentFormButton = document.querySelector("#clearStudentFormButton");
const adminFeedback = document.querySelector("#adminFeedback");
const studentStatusFilter = document.querySelector("#studentStatusFilter");
const studentSearch = document.querySelector("#studentSearch");
const studentList = document.querySelector("#studentList");

async function init() {
    await loadTestCards();
    if (activeUser?.rol && ROLE_LOGINS[activeUser.rol]) {
        roleSelect.value = activeUser.rol;
    }
    refreshSession();
    loginButton.addEventListener("click", login);
    roleSelect.addEventListener("change", () => {
        if (token && activeUser && roleSelect.value !== activeUser.rol) {
            clearSession();
        }
        if (!token) {
            renderProfile();
            updateRoleView();
        }
    });
    gateForm.addEventListener("submit", registerAccess);
    refreshDashboardButton.addEventListener("click", loadDashboard);
    refreshGuardianButton.addEventListener("click", loadGuardianDashboard);
    refreshStudentsButton.addEventListener("click", loadAdminPanel);
    studentForm.addEventListener("submit", saveStudent);
    clearStudentFormButton.addEventListener("click", resetStudentForm);
    studentStatusFilter.addEventListener("change", loadStudents);
    studentSearch.addEventListener("input", renderStudents);

    if (token && activeUser?.rol === "ADMINISTRADOR") {
        await loadAdminPanel();
    }
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
        if (activeUser.rol === "ADMINISTRADOR") {
            await loadAdminPanel();
        }
        if (activeUser.rol === "ACUDIENTE") {
            await loadGuardianDashboard();
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
        renderError("Primero inicia sesion como Porteria o Coordinador para usar la talanquera.");
        return;
    }

    if (!canUseGate()) {
        denyGate(code);
        renderError("Este perfil no puede registrar ingresos ni salidas. Selecciona Porteria o Coordinador.");
        return;
    }

    setBusy(true);
    resetGate();
    const mode = getAccessMode();

    try {
        const endpoint = mode === "ingresos" ? `${API_BASE}/talanquera/ingresos` : `${API_BASE}/talanquera/salidas`;
        const payload = {
            codigoTarjeta: code,
            observacion: `${mode === "ingresos" ? "Ingreso" : "Salida"} registrada desde simulador web`,
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
        renderSuccess(normalized);
        notifyGuardian(normalized);
    } catch (error) {
        if (error.status === 401 || error.status === 403) {
            clearSession();
            denyGate(code);
            renderError("La sesion expiro o el rol no tiene permisos.");
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

function normalizeAccessResponse(data, code, mode) {
    return {
        ...data,
        codigoTarjeta: data.codigoTarjeta || code,
        operacion: data.operacion || (mode === "ingresos" ? "INGRESO" : "SALIDA"),
        acudientesNotificados: data.acudientesNotificados || []
    };
}

function renderSuccess(data) {
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

function notifyGuardian(data) {
    const displayData = applyCardOverrides(data);
    const guardian = displayData.acudientesNotificados[0];
    const recipient = guardian ? guardian.correo : "acudiente";
    const operation = displayData.operacion === "SALIDA" ? "la salida" : "el ingreso";
    showToast(`Correo enviado a ${recipient} por ${operation} de ${displayData.estudiante}.`);
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

async function loadGuardianDashboard() {
    if (!token || activeUser?.rol !== "ACUDIENTE") {
        guardianContent.innerHTML = `<p class="hint">Inicia sesion como Acudiente para consultar el estado de tu estudiante.</p>`;
        return;
    }

    guardianContent.innerHTML = `<p class="hint">Cargando informacion del estudiante...</p>`;
    try {
        const estudiantes = await apiGet("/acudientes/me/estudiantes");
        renderGuardianDashboard(estudiantes);
    } catch (error) {
        guardianContent.innerHTML = `<p class="hint">${error.message}</p>`;
    }
}

async function apiGet(path) {
    return apiRequest(path);
}

async function apiRequest(path, options = {}) {
    const headers = {
        "Authorization": `Bearer ${token}`,
        ...(options.body ? { "Content-Type": "application/json" } : {})
    };
    const response = await fetch(`${API_BASE}${path}`, {
        method: options.method || "GET",
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined
    });
    return parseResponse(response);
}

async function loadAdminPanel() {
    if (!token || activeUser?.rol !== "ADMINISTRADOR") {
        return;
    }

    setAdminBusy(true);
    adminFeedback.textContent = "Cargando gestion de estudiantes...";
    try {
        await Promise.all([loadGrades(), loadStudents()]);
        adminFeedback.textContent = "Gestion lista.";
    } catch (error) {
        adminFeedback.textContent = error.message;
    } finally {
        setAdminBusy(false);
    }
}

async function loadGrades() {
    adminGrades = await apiGet("/grados?activo=true");
    renderGradeOptions();
}

async function loadStudents() {
    const status = studentStatusFilter.value;
    const query = status === "" ? "" : `?activo=${status}`;
    adminStudents = await apiGet(`/estudiantes${query}`);
    renderStudents();
}

function renderGradeOptions() {
    studentGradeId.innerHTML = adminGrades.map(grado => `
        <option value="${grado.id}">${escapeHtml(grado.nombre)} - ${escapeHtml(grado.nivel)}</option>
    `).join("");
}

async function saveStudent(event) {
    event.preventDefault();

    if (!token || activeUser?.rol !== "ADMINISTRADOR") {
        adminFeedback.textContent = "Inicia sesion como Administrador.";
        return;
    }

    const payload = {
        codigoEstudiantil: studentCode.value.trim().toUpperCase(),
        documento: studentDocument.value.trim(),
        nombres: studentNames.value.trim(),
        apellidos: studentLastNames.value.trim(),
        gradoId: Number(studentGradeId.value)
    };
    const editingId = studentId.value;

    setAdminBusy(true);
    try {
        const saved = await apiRequest(editingId ? `/estudiantes/${editingId}` : "/estudiantes", {
            method: editingId ? "PUT" : "POST",
            body: payload
        });
        adminFeedback.textContent = editingId
                ? `Estudiante actualizado: ${saved.codigoEstudiantil}.`
                : `Estudiante creado: ${saved.codigoEstudiantil}.`;
        showToast(adminFeedback.textContent);
        resetStudentForm();
        await loadStudents();
    } catch (error) {
        adminFeedback.textContent = error.message;
    } finally {
        setAdminBusy(false);
    }
}

function renderStudents() {
    const search = studentSearch.value.trim().toLowerCase();
    const filtered = adminStudents.filter(student => {
        if (!search) {
            return true;
        }
        return [
            student.codigoEstudiantil,
            student.documento,
            student.nombres,
            student.apellidos,
            student.grado
        ].some(value => String(value || "").toLowerCase().includes(search));
    });

    studentList.innerHTML = filtered.map(student => `
        <article class="student-card ${student.activo ? "" : "inactive"}">
            <div class="student-main">
                <strong>${escapeHtml(student.nombres)} ${escapeHtml(student.apellidos)}</strong>
                <span>${escapeHtml(student.codigoEstudiantil)} - ${escapeHtml(student.documento)}</span>
            </div>
            <div class="student-meta">
                <span>Grado</span>
                <strong>${escapeHtml(student.grado)}</strong>
            </div>
            <div class="student-meta">
                <span>Estado</span>
                <strong>${student.activo ? "Activo" : "Inactivo"}</strong>
            </div>
            <div class="student-actions">
                <button class="secondary-button compact-button" type="button" data-action="edit" data-id="${student.id}">Editar</button>
                <button class="danger-button compact-button" type="button" data-action="delete" data-id="${student.id}" ${student.activo ? "" : "disabled"}>Eliminar</button>
            </div>
        </article>
    `).join("") || `<p class="hint">No hay estudiantes para este filtro.</p>`;

    studentList.querySelectorAll("button[data-action]").forEach(button => {
        button.addEventListener("click", () => {
            const id = Number(button.dataset.id);
            if (button.dataset.action === "edit") {
                editStudent(id);
            } else {
                deleteStudent(id);
            }
        });
    });
}

function editStudent(id) {
    const student = adminStudents.find(item => item.id === id);
    if (!student) {
        adminFeedback.textContent = "Estudiante no encontrado en el listado actual.";
        return;
    }

    studentId.value = student.id;
    studentCode.value = student.codigoEstudiantil;
    studentDocument.value = student.documento;
    studentNames.value = student.nombres;
    studentLastNames.value = student.apellidos;
    studentGradeId.value = student.gradoId;
    studentFormTitle.textContent = "Editar estudiante";
    saveStudentButton.textContent = "Actualizar estudiante";
    adminFeedback.textContent = `Editando ${student.codigoEstudiantil}.`;
    studentCode.focus();
}

async function deleteStudent(id) {
    const student = adminStudents.find(item => item.id === id);
    if (!student) {
        adminFeedback.textContent = "Estudiante no encontrado en el listado actual.";
        return;
    }

    const ok = window.confirm(`Eliminar a ${student.nombres} ${student.apellidos}?`);
    if (!ok) {
        return;
    }

    setAdminBusy(true);
    try {
        await apiRequest(`/estudiantes/${id}`, { method: "DELETE" });
        adminFeedback.textContent = `Estudiante eliminado: ${student.codigoEstudiantil}.`;
        showToast(adminFeedback.textContent);
        if (studentId.value === String(id)) {
            resetStudentForm();
        }
        await loadStudents();
    } catch (error) {
        adminFeedback.textContent = error.message;
    } finally {
        setAdminBusy(false);
    }
}

function resetStudentForm() {
    studentForm.reset();
    studentId.value = "";
    if (adminGrades.length > 0) {
        studentGradeId.value = adminGrades[0].id;
    }
    studentFormTitle.textContent = "Nuevo estudiante";
    saveStudentButton.textContent = "Guardar estudiante";
    adminFeedback.textContent = "";
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

function renderGuardianDashboard(estudiantes) {
    guardianContent.innerHTML = `
        <div class="student-status-grid">
            ${estudiantes.map(student => {
                const present = student.estadoPermanencia === "DENTRO_DEL_PLANTEL";
                const arrival = student.ultimoTipoRegistro === "INGRESO" && student.ultimaFechaHora
                        ? formatDateTime(student.ultimaFechaHora)
                        : "Sin ingreso registrado";
                return `
                    <article class="student-status-card">
                        <div>
                            <span>Estudiante</span>
                            <strong>${student.estudiante}</strong>
                        </div>
                        <div>
                            <span>Grado</span>
                            <strong>${student.grado}</strong>
                        </div>
                        <div>
                            <span>Hora de llegada</span>
                            <strong>${arrival}</strong>
                        </div>
                        <div>
                            <span>Estado</span>
                            <strong class="${present ? "present-status" : "absent-status"}">${present ? "Presente" : "Ausente"}</strong>
                        </div>
                    </article>
                `;
            }).join("") || `<p class="hint">No hay estudiantes asociados a este acudiente.</p>`}
        </div>
    `;
}

function updateRoleView() {
    const effectiveRole = activeUser?.rol || roleSelect.value;
    const isAdmin = activeUser?.rol === "ADMINISTRADOR";
    const isCoordinator = activeUser?.rol === "COORDINADOR";
    const isGuardian = effectiveRole === "ACUDIENTE";
    adminPanel.classList.toggle("visible", isAdmin);
    dashboardPanel.classList.toggle("visible", isCoordinator);
    guardianPanel.classList.toggle("visible", activeUser?.rol === "ACUDIENTE");
    gateWorkspace.classList.toggle("hidden", isGuardian || isAdmin);
    if (!isAdmin) {
        studentList.innerHTML = "";
        adminFeedback.textContent = "";
    }
    if (!isCoordinator) {
        dashboardContent.innerHTML = "";
    }
    if (!isGuardian) {
        guardianContent.innerHTML = "";
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

function setAdminBusy(isBusy) {
    refreshStudentsButton.disabled = isBusy;
    saveStudentButton.disabled = isBusy;
    clearStudentFormButton.disabled = isBusy;
    saveStudentButton.textContent = isBusy
            ? "Guardando..."
            : (studentId.value ? "Actualizar estudiante" : "Guardar estudiante");
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

function formatDateTime(value) {
    return new Intl.DateTimeFormat("es-CO", {
        dateStyle: "short",
        timeStyle: "short"
    }).format(new Date(value));
}

function getAccessMode() {
    return document.querySelector("input[name='accessMode']:checked")?.value || "ingresos";
}

function escapeHtml(value) {
    return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
}

init();
