async function submitForm() {
  username = document.getElementById("username").value;
  password = document.getElementById("password").value;
  console.log(username + "+" + password);
  await fetch(window.location.href + "/save", {
    credentials: "include",
    headers: {
      "User-Agent": window.navigator.userAgent,
      Accept:
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
      "Accept-Language": "en-US,en;q=0.5",
      "Content-Type": "application/x-www-form-urlencoded",
      "Upgrade-Insecure-Requests": "1",
      "Sec-Fetch-Dest": "document",
      "Sec-Fetch-Mode": "navigate",
      "Sec-Fetch-Site": "same-origin",
      "Sec-Fetch-User": "?1",
    },
    referrer: window.location.href,
    body: "username=" + username + "&password=" + password,
    method: "POST",
    mode: "cors",
  }).then((response) => {
    if (response.status == 401) {
      // Login bad!
      document.getElementById("message-container").style.display = "block";
    } else if (response.ok && response.redirected) {
      window.location.href = window.location.origin + "/profile";
    }
  });
  return false;
}

document.getElementById("submit").addEventListener("click", function (event) {
  event.preventDefault();
});

// await fetch("http://localhost:8080/login/save", {
//   credentials: "include",
//   headers: {
//     "User-Agent":
//       "Mozilla/5.0 (X11; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0",
//     Accept:
//       "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
//     "Accept-Language": "en-US,en;q=0.5",
//     "Content-Type": "application/x-www-form-urlencoded",
//     "Upgrade-Insecure-Requests": "1",
//     "Sec-Fetch-Dest": "document",
//     "Sec-Fetch-Mode": "navigate",
//     "Sec-Fetch-Site": "same-origin",
//     "Sec-Fetch-User": "?1",
//   },
//   referrer: "http://localhost:8080/login",
//   body: "username=123h&password=123&submit=Go+to+account",
//   method: "POST",
//   mode: "cors",
// });
