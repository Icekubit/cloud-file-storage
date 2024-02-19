document.getElementById('logout').addEventListener('click', function(event) {
    event.preventDefault();
    fetch(location.host + '/logout', {method: 'POST'})
        .then(response => {
            if (response.ok) {
                window.location.href = '/';
            }
        })
        .catch(error => console.error('Error:', error));
});


