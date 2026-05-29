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
let adminGuardians = [];
let adminGuardianUsers = [];
let adminStudentRelations = new Map();
let adminUsers = [];
let activeAdminView = "students";
let noveltyStudents = [];
let lastAccessStudent = null;

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
const refreshAdminButton = document.querySelector("#refreshAdminButton");
const studentsAdminTab = document.querySelector("#studentsAdminTab");
const usersAdminTab = document.querySelector("#usersAdminTab");
const studentsAdminView = document.querySelector("#studentsAdminView");
const usersAdminView = document.querySelector("#usersAdminView");
const studentForm = document.querySelector("#studentForm");
const studentFormTitle = document.querySelector("#studentFormTitle");
const studentId = document.querySelector("#studentId");
const guardianId = document.querySelector("#guardianId");
const studentCode = document.querySelector("#studentCode");
const studentDocument = document.querySelector("#studentDocument");
const studentNames = document.querySelector("#studentNames");
const studentLastNames = document.querySelector("#studentLastNames");
const studentGradeId = document.querySelector("#studentGradeId");
const guardianDocument = document.querySelector("#guardianDocument");
const guardianPhone = document.querySelector("#guardianPhone");
const guardianNames = document.querySelector("#guardianNames");
const guardianLastNames = document.querySelector("#guardianLastNames");
const guardianEmail = document.querySelector("#guardianEmail");
const guardianRelationship = document.querySelector("#guardianRelationship");
const guardianUserSelect = document.querySelector("#guardianUserSelect");
const guardianMain = document.querySelector("#guardianMain");
const saveStudentButton = document.querySelector("#saveStudentButton");
const clearStudentFormButton = document.querySelector("#clearStudentFormButton");
const adminFeedback = document.querySelector("#adminFeedback");
const studentStatusFilter = document.querySelector("#studentStatusFilter");
const studentSearch = document.querySelector("#studentSearch");
const studentList = document.querySelector("#studentList");
const userForm = document.querySelector("#userForm");
const userFormTitle = document.querySelector("#userFormTitle");
const userId = document.querySelector("#userId");
const userNames = document.querySelector("#userNames");
const userLastNames = document.querySelector("#userLastNames");
const userEmail = document.querySelector("#userEmail");
const userRole = document.querySelector("#userRole");
const userPassword = document.querySelector("#userPassword");
const userActive = document.querySelector("#userActive");
const saveUserButton = document.querySelector("#saveUserButton");
const clearUserFormButton = document.querySelector("#clearUserFormButton");
const userFeedback = document.querySelector("#userFeedback");
const userStatusFilter = document.querySelector("#userStatusFilter");
const userSearch = document.querySelector("#userSearch");
const userList = document.querySelector("#userList");
const novedadPanel = document.querySelector("#novedadPanel");
const novedadRoleHint = document.querySelector("#novedadRoleHint");
const novedadForm = document.querySelector("#novedadForm");
const novedadStudentSelectField = document.querySelector("#novedadStudentSelectField");
const novedadStudentCodeField = document.querySelector("#novedadStudentCodeField");
const novedadStudentSelect = document.querySelector("#novedadStudentSelect");
const novedadStudentCode = document.querySelector("#novedadStudentCode");
const novedadType = document.querySelector("#novedadType");
const novedadDescription = document.querySelector("#novedadDescription");
const saveNovedadButton = document.querySelector("#saveNovedadButton");
const novedadFeedback = document.querySelector("#novedadFeedback");

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
    refreshAdminButton.addEventListener("click", loadAdminPanel);
    studentsAdminTab.addEventListener("click", () => switchAdminView("students"));
    usersAdminTab.addEventListener("click", () => switchAdminView("users"));
    studentForm.addEventListener("submit", saveStudent);
    clearStudentFormButton.addEventListener("click", resetStudentForm);
    studentStatusFilter.addEventListener("change", loadStudents);
    studentSearch.addEventListener("input", renderStudents);
    userForm.addEventListener("submit", saveUser);
    clearUserFormButton.addEventListener("click", resetUserForm);
    userStatusFilter.addEventListener("change", loadUsers);
    userSearch.addEventListener("input", renderUsers);
    novedadForm.addEventListener("submit", saveNovedad);

    if (token && activeUser?.rol === "ADMINISTRADOR") {
        await loadAdminPanel();
    }
    if (token && activeUser?.rol === "COORDINADOR") {
        await Promise.all([loadDashboard(), loadNovedadPanel()]);
    }
    if (token && activeUser?.rol === "PORTERIA") {
        await loadNovedadPanel();
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
            await Promise.all([loadDashboard(), loadNovedadPanel()]);
        }
        if (activeUser.rol === "ADMINISTRADOR") {
            await loadAdminPanel();
        }
        if (activeUser.rol === "PORTERIA") {
            await loadNovedadPanel();
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
        lastAccessStudent = {
            id: normalized.estudianteId,
            code: normalized.codigoTarjeta,
            name: normalized.estudiante
        };
        prefillNovedadFromAccess(lastAccessStudent);
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
        const validationErrors = Array.isArray(data.errores) && data.errores.length > 0
            ? ` ${data.errores.join(" ")}`
            : "";
        const error = new Error(`${data.mensaje || data.message || data.detail || data.error || "No fue posible completar la operacion."}${validationErrors}`);
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

async function loadNovedadPanel() {
    if (!token || !canCreateNovedad()) {
        return;
    }

    const isCoordinator = activeUser?.rol === "COORDINADOR";
    novedadRoleHint.textContent = isCoordinator
            ? "Selecciona estudiante y registra el seguimiento"
            : "Registra por carnet o documento";
    novedadStudentSelectField.hidden = !isCoordinator;
    novedadStudentCodeField.hidden = isCoordinator;
    novedadStudentSelect.required = isCoordinator;
    novedadStudentCode.required = !isCoordinator;

    if (isCoordinator) {
        novedadFeedback.textContent = "Cargando estudiantes...";
        try {
            noveltyStudents = await apiGet("/estudiantes?activo=true");
            renderNovedadStudentOptions();
            if (lastAccessStudent?.id) {
                novedadStudentSelect.value = String(lastAccessStudent.id);
            }
            novedadFeedback.textContent = "";
        } catch (error) {
            novedadFeedback.textContent = error.message;
        }
        return;
    }

    if (lastAccessStudent?.code) {
        novedadStudentCode.value = lastAccessStudent.code;
    }
    novedadFeedback.textContent = "";
}

function renderNovedadStudentOptions() {
    novedadStudentSelect.innerHTML = `
        <option value="">Selecciona estudiante</option>
        ${noveltyStudents.map(student => `
            <option value="${student.id}">${escapeHtml(student.codigoEstudiantil)} - ${escapeHtml(student.nombres)} ${escapeHtml(student.apellidos)} (${escapeHtml(student.grado)})</option>
        `).join("")}
    `;
}

function prefillNovedadFromAccess(student) {
    if (!student || !canCreateNovedad()) {
        return;
    }
    if (activeUser?.rol === "COORDINADOR" && student.id) {
        novedadStudentSelect.value = String(student.id);
    }
    if (activeUser?.rol === "PORTERIA" && student.code) {
        novedadStudentCode.value = student.code;
    }
}

async function saveNovedad(event) {
    event.preventDefault();

    if (!token || !canCreateNovedad()) {
        novedadFeedback.textContent = "Inicia sesion como Porteria o Coordinador.";
        return;
    }

    const payload = {
        tipoNovedad: novedadType.value,
        descripcion: novedadDescription.value.trim()
    };
    if (activeUser?.rol === "COORDINADOR") {
        payload.estudianteId = Number(novedadStudentSelect.value);
    } else {
        payload.identificadorEstudiante = novedadStudentCode.value.trim().toUpperCase();
    }

    setNovedadBusy(true);
    try {
        const saved = await apiRequest("/novedades", {
            method: "POST",
            body: payload
        });
        novedadFeedback.textContent = `Novedad creada para ${saved.estudiante}.`;
        showToast(novedadFeedback.textContent);
        novedadDescription.value = "";
        novedadType.value = "OBSERVACION_SEGURIDAD";
        if (activeUser?.rol === "COORDINADOR") {
            await loadDashboard();
        }
    } catch (error) {
        novedadFeedback.textContent = error.message;
    } finally {
        setNovedadBusy(false);
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
    adminFeedback.textContent = "Cargando gestion administrativa...";
    userFeedback.textContent = "Cargando usuarios...";
    try {
        await Promise.all([loadGrades(), loadGuardians(), loadGuardianUsers(), loadStudents(), loadUsers()]);
        adminFeedback.textContent = "Gestion lista.";
        userFeedback.textContent = "Gestion lista.";
    } catch (error) {
        adminFeedback.textContent = error.message;
        userFeedback.textContent = error.message;
    } finally {
        setAdminBusy(false);
    }
}

function switchAdminView(view) {
    activeAdminView = view;
    const showStudents = view === "students";
    studentsAdminTab.classList.toggle("active", showStudents);
    usersAdminTab.classList.toggle("active", !showStudents);
    studentsAdminView.classList.toggle("active", showStudents);
    usersAdminView.classList.toggle("active", !showStudents);
}

async function loadGrades() {
    adminGrades = await apiGet("/grados?activo=true");
    renderGradeOptions();
}

async function loadGuardians() {
    adminGuardians = await apiGet("/acudientes?activo=true");
}

async function loadGuardianUsers(selectedId = guardianUserSelect.value) {
    const activeUsers = await apiGet("/usuarios?activo=true");
    adminGuardianUsers = activeUsers.filter(user => user.rol === "ACUDIENTE");
    renderGuardianUserOptions(selectedId);
}

async function loadStudents() {
    const status = studentStatusFilter.value;
    const query = status === "" ? "" : `?activo=${status}`;
    adminStudents = await apiGet(`/estudiantes${query}`);
    await loadStudentRelations();
    renderStudents();
}

async function loadStudentRelations() {
    const entries = await Promise.all(adminStudents.map(async student => {
        try {
            return [student.id, await apiGet(`/estudiantes/${student.id}/acudientes`)];
        } catch (error) {
            return [student.id, []];
        }
    }));
    adminStudentRelations = new Map(entries);
}

function renderGradeOptions() {
    studentGradeId.innerHTML = adminGrades.map(grado => `
        <option value="${grado.id}">${escapeHtml(grado.nombre)} - ${escapeHtml(grado.nivel)}</option>
    `).join("");
}

function renderGuardianUserOptions(selectedId = "") {
    const normalizedSelectedId = selectedId ? String(selectedId) : "";
    const linkedUserMissing = normalizedSelectedId
            && !adminGuardianUsers.some(user => String(user.id) === normalizedSelectedId);
    guardianUserSelect.innerHTML = `
        <option value="">Sin usuario de acceso</option>
        ${adminGuardianUsers.map(user => `
            <option value="${user.id}">${escapeHtml(user.nombres)} ${escapeHtml(user.apellidos)} - ${escapeHtml(user.correo)}</option>
        `).join("")}
        ${linkedUserMissing ? `<option value="${escapeHtml(normalizedSelectedId)}">Usuario actual no disponible</option>` : ""}
    `;
    guardianUserSelect.value = normalizedSelectedId;
}

async function saveStudent(event) {
    event.preventDefault();

    if (!token || activeUser?.rol !== "ADMINISTRADOR") {
        adminFeedback.textContent = "Inicia sesion como Administrador.";
        return;
    }

    const studentPayload = {
        codigoEstudiantil: studentCode.value.trim().toUpperCase(),
        documento: studentDocument.value.trim(),
        nombres: studentNames.value.trim(),
        apellidos: studentLastNames.value.trim(),
        gradoId: Number(studentGradeId.value)
    };
    const guardianPayload = {
        documento: guardianDocument.value.trim(),
        nombres: guardianNames.value.trim(),
        apellidos: guardianLastNames.value.trim(),
        telefono: guardianPhone.value.trim(),
        correo: guardianEmail.value.trim(),
        usuarioId: guardianUserSelect.value ? Number(guardianUserSelect.value) : null
    };
    const editingId = studentId.value;
    const editingGuardianId = guardianId.value;

    setAdminBusy(true);
    try {
        const existingGuardian = editingGuardianId
                ? null
                : findExistingGuardian(guardianPayload.documento, guardianPayload.correo);
        const targetGuardianId = editingGuardianId || existingGuardian?.id || "";
        if (!guardianPayload.usuarioId && existingGuardian?.usuarioId) {
            guardianPayload.usuarioId = existingGuardian.usuarioId;
        }

        const saved = await apiRequest(editingId ? `/estudiantes/${editingId}` : "/estudiantes", {
            method: editingId ? "PUT" : "POST",
            body: studentPayload
        });
        const savedGuardian = targetGuardianId
                ? await apiRequest(`/acudientes/${targetGuardianId}`, {
                    method: "PUT",
                    body: guardianPayload
                })
                : await apiRequest("/acudientes", {
                    method: "POST",
                    body: guardianPayload
                });
        const relationPayload = {
            acudienteId: savedGuardian.id,
            parentesco: guardianRelationship.value.trim(),
            responsablePrincipal: guardianMain.checked
        };
        if (editingGuardianId) {
            await apiRequest(`/estudiantes/${saved.id}/acudientes/${savedGuardian.id}`, {
                method: "PUT",
                body: relationPayload
            });
        } else {
            await apiRequest(`/estudiantes/${saved.id}/acudientes`, {
                method: "POST",
                body: relationPayload
            });
        }
        adminFeedback.textContent = editingId
                ? `Estudiante y acudiente actualizados: ${saved.codigoEstudiantil}.`
                : `Estudiante y acudiente creados: ${saved.codigoEstudiantil}.`;
        showToast(adminFeedback.textContent);
        resetStudentForm();
        await Promise.all([loadGuardians(), loadStudents()]);
    } catch (error) {
        adminFeedback.textContent = error.message;
    } finally {
        setAdminBusy(false);
    }
}

function renderStudents() {
    const search = studentSearch.value.trim().toLowerCase();
    const filtered = adminStudents.filter(student => {
        const relation = primaryRelation(student.id);
        const guardian = relation?.acudiente;
        if (!search) {
            return true;
        }
        return [
            student.codigoEstudiantil,
            student.documento,
            student.nombres,
            student.apellidos,
            student.grado,
            guardian?.documento,
            guardian?.nombres,
            guardian?.apellidos,
            guardian?.correo
        ].some(value => String(value || "").toLowerCase().includes(search));
    });

    studentList.innerHTML = filtered.map(student => {
        const relation = primaryRelation(student.id);
        const guardian = relation?.acudiente;
        return `
        <article class="student-card student-card-rich ${student.activo ? "" : "inactive"}">
            <div class="student-main">
                <strong>${escapeHtml(student.nombres)} ${escapeHtml(student.apellidos)}</strong>
                <span>${escapeHtml(student.codigoEstudiantil)} - ${escapeHtml(student.documento)}</span>
                <small>${escapeHtml(student.grado)} - ${student.activo ? "Activo" : "Inactivo"}</small>
            </div>
            <div class="student-meta">
                <span>Acudiente</span>
                <strong>${guardian ? `${escapeHtml(guardian.nombres)} ${escapeHtml(guardian.apellidos)}` : "Sin acudiente"}</strong>
                <small>${guardian ? `${escapeHtml(relation.parentesco)} - ${escapeHtml(guardian.telefono)}` : "Pendiente"}</small>
                <small>${guardian ? escapeHtml(guardian.correo) : ""}</small>
                <small>${guardian?.usuarioCorreo ? `Login: ${escapeHtml(guardian.usuarioCorreo)}` : ""}</small>
            </div>
            <div class="student-actions">
                <button class="secondary-button compact-button" type="button" data-action="edit" data-id="${student.id}">Editar</button>
                <button class="danger-button compact-button" type="button" data-action="delete" data-id="${student.id}" ${student.activo ? "" : "disabled"}>Eliminar</button>
            </div>
        </article>
    `;
    }).join("") || `<p class="hint">No hay estudiantes para este filtro.</p>`;

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

async function editStudent(id) {
    const student = adminStudents.find(item => item.id === id);
    if (!student) {
        adminFeedback.textContent = "Estudiante no encontrado en el listado actual.";
        return;
    }

    if (!adminStudentRelations.has(id)) {
        adminStudentRelations.set(id, await apiGet(`/estudiantes/${id}/acudientes`));
    }
    const relation = primaryRelation(id);
    const guardian = relation?.acudiente;
    studentId.value = student.id;
    guardianId.value = guardian?.id || "";
    studentCode.value = student.codigoEstudiantil;
    studentDocument.value = student.documento;
    studentNames.value = student.nombres;
    studentLastNames.value = student.apellidos;
    studentGradeId.value = student.gradoId;
    guardianDocument.value = guardian?.documento || "";
    guardianPhone.value = guardian?.telefono || "";
    guardianNames.value = guardian?.nombres || "";
    guardianLastNames.value = guardian?.apellidos || "";
    guardianEmail.value = guardian?.correo || "";
    guardianRelationship.value = relation?.parentesco || "";
    renderGuardianUserOptions(guardian?.usuarioId || "");
    guardianMain.checked = relation?.responsablePrincipal ?? true;
    studentFormTitle.textContent = "Editar estudiante";
    saveStudentButton.textContent = "Actualizar estudiante";
    adminFeedback.textContent = guardian
            ? `Editando ${student.codigoEstudiantil} con acudiente ${guardian.correo}.`
            : `Editando ${student.codigoEstudiantil}; agrega un acudiente.`;
    studentCode.focus();
}

async function deleteStudent(id) {
    const student = adminStudents.find(item => item.id === id);
    if (!student) {
        adminFeedback.textContent = "Estudiante no encontrado en el listado actual.";
        return;
    }

    const relation = primaryRelation(id);
    const guardian = relation?.acudiente;
    const ok = window.confirm(
            `Eliminar a ${student.nombres} ${student.apellidos}?`
            + (guardian ? `\nAcudiente asociado: ${guardian.nombres} ${guardian.apellidos}.` : "")
    );
    if (!ok) {
        return;
    }

    setAdminBusy(true);
    try {
        await apiRequest(`/estudiantes/${id}`, { method: "DELETE" });
        adminFeedback.textContent = `Estudiante eliminado: ${student.codigoEstudiantil}. El acudiente queda disponible para otros estudiantes.`;
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
    guardianId.value = "";
    if (adminGrades.length > 0) {
        studentGradeId.value = adminGrades[0].id;
    }
    renderGuardianUserOptions("");
    guardianMain.checked = true;
    studentFormTitle.textContent = "Nuevo estudiante";
    saveStudentButton.textContent = "Guardar estudiante";
    adminFeedback.textContent = "";
}

function primaryRelation(studentIdValue) {
    const relations = adminStudentRelations.get(studentIdValue) || [];
    return relations.find(relation => relation.responsablePrincipal) || relations[0] || null;
}

function findExistingGuardian(documento, correo) {
    const normalizedDocumento = documento.trim().toLowerCase();
    const normalizedCorreo = correo.trim().toLowerCase();
    return adminGuardians.find(guardian =>
        String(guardian.documento || "").toLowerCase() === normalizedDocumento
        || String(guardian.correo || "").toLowerCase() === normalizedCorreo
    );
}

async function loadUsers() {
    const status = userStatusFilter.value;
    const query = status === "" ? "" : `?activo=${status}`;
    adminUsers = await apiGet(`/usuarios${query}`);
    renderUsers();
}

async function saveUser(event) {
    event.preventDefault();

    if (!token || activeUser?.rol !== "ADMINISTRADOR") {
        userFeedback.textContent = "Inicia sesion como Administrador.";
        return;
    }

    const editingId = userId.value;
    const password = userPassword.value.trim();
    if (!editingId && !password) {
        userFeedback.textContent = "El password es obligatorio para crear usuarios.";
        return;
    }

    const payload = {
        nombres: userNames.value.trim(),
        apellidos: userLastNames.value.trim(),
        correo: userEmail.value.trim(),
        rol: userRole.value
    };
    if (editingId) {
        payload.activo = userActive.checked;
        payload.password = password || null;
    } else {
        payload.password = password;
    }

    setAdminBusy(true);
    try {
        const saved = await apiRequest(editingId ? `/usuarios/${editingId}` : "/usuarios", {
            method: editingId ? "PUT" : "POST",
            body: payload
        });
        userFeedback.textContent = editingId
                ? `Usuario actualizado: ${saved.correo}.`
                : `Usuario creado: ${saved.correo}.`;
        showToast(userFeedback.textContent);
        resetUserForm();
        await Promise.all([loadUsers(), loadGuardianUsers()]);
    } catch (error) {
        userFeedback.textContent = error.message;
    } finally {
        setAdminBusy(false);
    }
}

function renderUsers() {
    const search = userSearch.value.trim().toLowerCase();
    const filtered = adminUsers.filter(user => {
        if (!search) {
            return true;
        }
        return [
            user.nombres,
            user.apellidos,
            user.correo,
            user.rol
        ].some(value => String(value || "").toLowerCase().includes(search));
    });

    userList.innerHTML = filtered.map(user => `
        <article class="student-card user-card ${user.activo ? "" : "inactive"}">
            <div class="student-main">
                <strong>${escapeHtml(user.nombres)} ${escapeHtml(user.apellidos)}</strong>
                <span>${escapeHtml(user.correo)}</span>
            </div>
            <div class="student-meta">
                <span>Rol</span>
                <strong>${roleLabel(user.rol)}</strong>
            </div>
            <div class="student-meta">
                <span>Estado</span>
                <strong>${user.activo ? "Activo" : "Inactivo"}</strong>
            </div>
            <div class="student-actions">
                <button class="secondary-button compact-button" type="button" data-user-action="edit" data-id="${user.id}">Editar</button>
                <button class="danger-button compact-button" type="button" data-user-action="delete" data-id="${user.id}" ${user.correo === activeUser?.correo || !user.activo ? "disabled" : ""}>Eliminar</button>
            </div>
        </article>
    `).join("") || `<p class="hint">No hay usuarios para este filtro.</p>`;

    userList.querySelectorAll("button[data-user-action]").forEach(button => {
        button.addEventListener("click", () => {
            const id = Number(button.dataset.id);
            if (button.dataset.userAction === "edit") {
                editUser(id);
            } else {
                deleteUser(id);
            }
        });
    });
}

function editUser(id) {
    const user = adminUsers.find(item => item.id === id);
    if (!user) {
        userFeedback.textContent = "Usuario no encontrado en el listado actual.";
        return;
    }

    userId.value = user.id;
    userNames.value = user.nombres;
    userLastNames.value = user.apellidos;
    userEmail.value = user.correo;
    userRole.value = user.rol;
    userPassword.value = "";
    userPassword.required = false;
    userActive.checked = user.activo;
    userFormTitle.textContent = "Editar usuario";
    saveUserButton.textContent = "Actualizar usuario";
    userFeedback.textContent = `Editando ${user.correo}.`;
    userNames.focus();
}

async function deleteUser(id) {
    const user = adminUsers.find(item => item.id === id);
    if (!user) {
        userFeedback.textContent = "Usuario no encontrado en el listado actual.";
        return;
    }

    if (user.correo === activeUser?.correo) {
        userFeedback.textContent = "No puedes eliminar el usuario con la sesion activa.";
        return;
    }

    const ok = window.confirm(`Eliminar usuario ${user.correo}?`);
    if (!ok) {
        return;
    }

    setAdminBusy(true);
    try {
        await apiRequest(`/usuarios/${id}`, { method: "DELETE" });
        userFeedback.textContent = `Usuario eliminado: ${user.correo}.`;
        showToast(userFeedback.textContent);
        if (userId.value === String(id)) {
            resetUserForm();
        }
        await Promise.all([loadUsers(), loadGuardianUsers()]);
    } catch (error) {
        userFeedback.textContent = error.message;
    } finally {
        setAdminBusy(false);
    }
}

function resetUserForm() {
    userForm.reset();
    userId.value = "";
    userActive.checked = true;
    userPassword.required = true;
    userFormTitle.textContent = "Nuevo usuario";
    saveUserButton.textContent = "Guardar usuario";
    userFeedback.textContent = "";
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
    const hasNovedadAccess = canCreateNovedad();
    adminPanel.classList.toggle("visible", isAdmin);
    dashboardPanel.classList.toggle("visible", isCoordinator);
    novedadPanel.classList.toggle("visible", hasNovedadAccess);
    guardianPanel.classList.toggle("visible", activeUser?.rol === "ACUDIENTE");
    gateWorkspace.classList.toggle("hidden", isGuardian || isAdmin);
    if (!isAdmin) {
        studentList.innerHTML = "";
        userList.innerHTML = "";
        adminFeedback.textContent = "";
        userFeedback.textContent = "";
    }
    if (!isCoordinator) {
        dashboardContent.innerHTML = "";
    }
    if (!isGuardian) {
        guardianContent.innerHTML = "";
    }
    if (!hasNovedadAccess) {
        novedadFeedback.textContent = "";
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
    refreshAdminButton.disabled = isBusy;
    saveStudentButton.disabled = isBusy;
    clearStudentFormButton.disabled = isBusy;
    saveUserButton.disabled = isBusy;
    clearUserFormButton.disabled = isBusy;
    saveStudentButton.textContent = isBusy
            ? "Guardando..."
            : (studentId.value ? "Actualizar estudiante" : "Guardar estudiante");
    saveUserButton.textContent = isBusy
            ? "Guardando..."
            : (userId.value ? "Actualizar usuario" : "Guardar usuario");
}

function setNovedadBusy(isBusy) {
    saveNovedadButton.disabled = isBusy;
    saveNovedadButton.textContent = isBusy ? "Creando..." : "Crear novedad";
}

function canUseGate() {
    return activeUser?.rol === "PORTERIA" || activeUser?.rol === "COORDINADOR";
}

function canCreateNovedad() {
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
