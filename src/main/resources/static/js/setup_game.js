document.addEventListener("DOMContentLoaded", main);

function main() {
    document.querySelector("select#opponent-name").addEventListener("input", function() {
        document.querySelector("#submit").removeAttribute("disabled");
    });
}