document.getElementById('logout').addEventListener('click', function(event) {
    event.preventDefault();
    const pathName = window.location.pathname;
    const logoutUrl = window.location.origin + pathName.substring(0, pathName.indexOf('search/')) + 'logout'
    fetch(logoutUrl, {method: 'POST'})
        .then(response => {
            if (response.ok) {
                window.location.href = window.location.origin + window.location.pathname;
            }
        })
        .catch(error => console.error('Error:', error));
});


