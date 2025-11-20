// web/js/ai-chat.js

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("chat-form");
    const input = document.getElementById("chat-input");
    const chatWindow = document.getElementById("chat-window");

    if (!form || !input || !chatWindow) {
        // Falls das Dashboard ohne Chat geladen wird, einfach sauber rausgehen
        return;
    }

    function addMessage(role, text) {
        const wrapper = document.createElement("div");
        wrapper.className = "chat-message " + role; // "user" oder "assistant"
        wrapper.innerText = text;
        chatWindow.appendChild(wrapper);
        chatWindow.scrollTop = chatWindow.scrollHeight;
    }

    // Optional: Begrüßungsnachricht
    addMessage(
        "assistant",
        "Hallo! Ich bin dein Smart Trading Assistant. Frag mich z.B.:\n" +
        "• Wie ist mein heutiger Gewinn/Verlust?\n" +
        "• Welche offenen Orders sind riskant?\n" +
        "• Wie hat sich der Nasdaq heute entwickelt?"
    );

    form.addEventListener("submit", function (e) {
        e.preventDefault();
        const message = input.value.trim();
        if (!message) return;

        // User-Nachricht anzeigen
        addMessage("user", message);
        input.value = "";
        input.disabled = true;

        fetch("ai-chat", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: "message=" + encodeURIComponent(message)
        })
            .then(function (resp) {
                if (!resp.ok) {
                    throw new Error("HTTP-Status: " + resp.status);
                }
                return resp.json();
            })
            .then(function (data) {
                if (data.reply) {
                    addMessage("assistant", data.reply);
                } else if (data.error) {
                    addMessage("assistant", "Fehler: " + data.error);
                } else {
                    addMessage("assistant", "Unerwartete Antwort vom Server.");
                }
            })
            .catch(function (err) {
                console.error(err);
                addMessage(
                    "assistant",
                    "Es ist ein Fehler bei der Anfrage aufgetreten. " +
                    "Bitte versuche es später erneut."
                );
            })
            .finally(function () {
                input.disabled = false;
                input.focus();
            });
    });
});
