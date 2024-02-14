const dropZone = document.getElementById('dropZone');

dropZone.addEventListener('dragover', (event) => {
    event.preventDefault();
    dropZone.classList.add('drop-zone--over');
});

dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('drop-zone--over');
});

dropZone.addEventListener('drop', async (event) => {
    event.preventDefault();
    dropZone.classList.remove('drop-zone--over');
    const items = event.dataTransfer.items;
    const fetchPromises = [];
    let completedPromises = 0;

    for (let i = 0; i < items.length; i++) {
        pushFetchToArray(items[i].webkitGetAsEntry());
    }

    await Promise.all(fetchPromises);

    function pushFetchToArray(entry) {
        if (entry !== null && entry.isFile) {
            const urlParams = new URLSearchParams(window.location.search);
            const currentPath = urlParams.get('path') !== null ? urlParams.get('path') : '';

            const fullPath = entry.fullPath.slice(1);
            const parts = fullPath.split('/');
            let pathToFile = parts.slice(0, -1).join('/');

            if (currentPath !== '' && pathToFile !== '') {
                pathToFile = currentPath + '/' + pathToFile;
            }

            entry.file((file) => {
                pushUploadFileFetchToArray(file, pathToFile);
            });
        }

        if (entry !== null && entry.isDirectory) {
            const urlParams = new URLSearchParams(window.location.search);
            const currentPath = urlParams.get('path') !== null ? urlParams.get('path') : '';
            const fullPath = entry.fullPath.slice(1);  // get fullPath without first slash
            const parts = fullPath.split('/');

            let pathToNewFolder = parts.slice(0, -1).join('/');

            if (currentPath !== '' && pathToNewFolder !== '') {
                pathToNewFolder = currentPath + '/' + pathToNewFolder;
            }
            const newFolderName = parts[parts.length - 1];
            pushCreateFolderFetchToArray(pathToNewFolder, newFolderName);

            const dirReader = entry.createReader();
            dirReader.readEntries((entries) => {
                entries.forEach((entry) => {
                    pushFetchToArray(entry);
                });
            });
        }
    }

    function pushCreateFolderFetchToArray(currentPath, objectName) {
        const url = window.location.origin + '/folder';
        const formData = new FormData();
        formData.append('currentPath', currentPath);
        formData.append('objectName', objectName);
        const createFolderFetch = fetch(url, {
            method: 'POST',
            body: formData
        }).then(() => {
            completedPromises++;
            checkAllPromisesCompleted();
        });

        fetchPromises.push(createFolderFetch);
    }

    function pushUploadFileFetchToArray(file, pathToFile) {
        const url = window.location.origin + '/file/upload';
        const formData = new FormData();
        formData.append('file', file);
        formData.append('currentPath', pathToFile);

        const fetchPromise = fetch(url, {
            method: 'POST',
            body: formData
        }).then(() => {
            completedPromises++;
            checkAllPromisesCompleted();
        });
        fetchPromises.push(fetchPromise);
    }

    function checkAllPromisesCompleted() {
        if (completedPromises === fetchPromises.length) {
            location.reload();
        }
    }
});