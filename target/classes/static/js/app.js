// app.js

let stompClient = null;
let segmentsList = [];

// Ejecutar cuando el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', function () {
    // Asignar manejadores de eventos sin atributos inline
    document.getElementById("uploadForm").addEventListener("submit", submitForm);
    document.getElementById("mailForm").addEventListener("submit", sendMail);
    document.getElementById("fileInput").addEventListener("change", storeFileName);

    // Conectarse al WebSocket
    connectWebSocket();
});

// Conectar al WebSocket (STOMP)
function connectWebSocket() {
    const socket = new SockJS('/ws-split');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        const sessionId = document.getElementById("sessionId").value;
        stompClient.subscribe('/topic/progress/' + sessionId, (message) => {
            handleProgressUpdate(message.body);
        });
    });
}

// Al seleccionar el archivo, guardar su nombre y habilitar el botón
function storeFileName(event) {
    const file = event.target.files[0];
    if (file) {
        document.getElementById("originalFileName").value = file.name;
        document.getElementById("submitButton").disabled = false;
    } else {
        document.getElementById("submitButton").disabled = true;
    }
}

// Enviar el formulario de subida
async function submitForm(event) {
    event.preventDefault();
    document.getElementById("submitButton").disabled = true;
    segmentsList = [];

    const form = document.getElementById("uploadForm");
    const formData = new FormData(form);

    try {
        const response = await fetch('/upload', {
            method: 'POST',
            body: formData
        });

        let result;
        if (!response.ok) {
            const errorJson = await response.json();
            result = errorJson.message;
            alert("Error al subir archivo: " + result);
        } else {
            result = await response.text();
            console.log('Resultado upload:', result);
            document.getElementById("progressText").textContent = "Procesando...";
            document.getElementById("progressBar").style.width = '0%';
        }
    } catch (error) {
        console.error('Error al subir archivo:', error);
    }
    return false;
}

// Manejar notificaciones de progreso
function handleProgressUpdate(msg) {
    console.log("Progreso recibido: " + msg);
    const progressText = document.getElementById("progressText");
    const progressBar = document.getElementById("progressBar");

    if (msg === 'DONE') {
        progressText.textContent = "¡Segmentación completada!";
        progressBar.style.width = '100%';

        const originalName = document.getElementById("originalFileName").value;
        fetchSegmentsList(originalName)
            .then(() => {
                document.getElementById("fileInput").value = null;
                document.getElementById("submitButton").disabled = true;
            });
    } else {
        const percent = parseFloat(msg);
        if (!isNaN(percent)) {
            progressText.textContent = "Progreso: " + percent.toFixed(2) + "%";
            progressBar.style.width = percent + '%';
        }
    }
}

// Obtener la lista de segmentos desde el backend
async function fetchSegmentsList(originalName) {
    if (!originalName) return;
    try {
        const response = await fetch('/listSegments?originalName=' + encodeURIComponent(originalName));
        if (!response.ok) {
            const errorJson = await response.json();
            throw new Error(errorJson.message || `Error HTTP: ${response.status}`);
        }
        segmentsList = await response.json();
        renderSegments(segmentsList);
    } catch (err) {
        console.error("Error al listar segmentos:", err);
        document.getElementById("segmentList").innerHTML =
            `<li class='list-group-item text-danger'>${err.message}</li>`;
    }
}

// Renderizar los enlaces de los segmentos como botones
function renderSegments(segments) {
    const segmentListElem = document.getElementById("segmentList");
    segmentListElem.innerHTML = "";

    // Crear un contenedor para los botones, por ejemplo, un div con clase "d-flex flex-wrap"
    const container = document.createElement("div");
    container.classList.add("d-flex", "flex-wrap", "gap-2"); // 'gap-2' añade espacio entre botones

    segments.forEach(segName => {
        const btn = document.createElement("a");
        btn.href = "/download/" + segName;
        btn.textContent = segName;
        // Usa clases de botón para darle un estilo moderno, puedes cambiar btn-outline-primary por otro color
        btn.classList.add("btn", "btn-outline-primary", "btn-sm");
        container.appendChild(btn);
    });

    segmentListElem.appendChild(container);
}


// Enviar los segmentos por correo
async function sendMail(event) {
    event.preventDefault();
    const toEmail = document.getElementById("toEmail").value;

    if (!segmentsList || segmentsList.length === 0) {
        document.getElementById("mailStatus").textContent = "No hay segmentos para enviar.";
        return false;
    }

    document.getElementById("mailLoading").classList.remove("hidden");
    document.getElementById("sendMailButton").disabled = true;
    document.getElementById("mailStatus").textContent = "";

    const formData = new URLSearchParams();
    formData.append("toEmail", toEmail);
    segmentsList.forEach(seg => {
        formData.append("segmentNames", seg);
    });

    try {
        const response = await fetch('/sendEmail', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });

        let result;
        if (!response.ok) {
            const errorJson = await response.json();
            result = errorJson.message;
        } else {
            result = await response.text();
        }
        document.getElementById("mailStatus").textContent = result;
    } catch (error) {
        document.getElementById("mailStatus").textContent = "Error enviando correo: " + error;
    } finally {
        document.getElementById("mailLoading").classList.add("hidden");
        document.getElementById("sendMailButton").disabled = false;
    }
    return false;
}
