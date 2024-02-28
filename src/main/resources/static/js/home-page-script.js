const dropZone = document.getElementById('dropZone');
const logout = document.getElementById('logout')

const urlParams = new URLSearchParams(window.location.search);
const currentPath = urlParams.get('path') !== null ? urlParams.get('path') : '';

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
        const url = window.location.origin + window.location.pathname + 'folder';
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
        const url = window.location.origin + window.location.pathname + 'file/upload';
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
logout.addEventListener("click", function (event) {
    event.preventDefault();
    fetch('logout', {method: 'POST'})
        .then(response => {
            if (response.ok) {
                window.location.href = window.location.origin + window.location.pathname;
            }
        })
        .catch(error => console.error('Error:', error));
});

document.addEventListener("DOMContentLoaded", function () {

    const folderIcons = document.getElementsByClassName("folder-icon");
    const fileIcons = document.getElementsByClassName("file-icon");
    const customContextMenu = document.getElementById("customContextMenu");

    document.addEventListener('contextmenu', (event) => {
        if (!event.target.classList.contains('folder-icon') && !event.target.classList.contains('file-icon')) {
            hideContextMenu();
        }
    });

    Array.from(folderIcons).forEach(function (folderIcon) {
        const objectName = folderIcon.parentElement.querySelector('p').textContent;
        const pathToObject = folderIcon.parentElement.querySelector('.relativePath').textContent

        folderIcon.addEventListener("contextmenu", function (event) {
            event.preventDefault();

            customContextMenu.style.display = "block";
            customContextMenu.style.left = event.pageX + "px";
            customContextMenu.style.top = event.pageY + "px";

            const contextMenuDownload = document.getElementById('contextMenuDownload');
            contextMenuDownload.addEventListener("click", function (event) {
                document.getElementById('downloadFolderForm').querySelector("[name='pathToFolder']").value = pathToObject;
                document.getElementById('downloadFolderForm').submit();
            })

            const contextMenuRename = document.getElementById('contextMenuRename');
            contextMenuRename.addEventListener('click', () => {
                document.getElementById('renameInput').value = objectName;
            });

            const contextMenuDelete = document.getElementById('contextMenuDelete');
            contextMenuDelete.addEventListener("click", function (event) {

                document.getElementById("deleteForm").querySelector("[name='objectName']").value = objectName + '/';
                document.getElementById("deleteForm").querySelector("[name='currentPath']").value = currentPath;

                document.getElementById("deleteForm").submit();
            })

            const renameButton = document.getElementById('renameButton');
            const renameInput = document.getElementById('renameInput');
            const renameForm = document.getElementById('renameForm');

            renameButton.addEventListener("click", function() {
                const newName = renameInput.value;
                renameForm.querySelector("input[name='objectName']").value = newName;
                renameForm.querySelector("input[name='relativePathToObject']").value = pathToObject;
                renameForm.querySelector("input[name='currentPath']").value = currentPath;
                renameForm.submit();
            });


            // Hide the context menu when clicking elsewhere
            document.addEventListener("click", hideContextMenu);
        });

        folderIcon.addEventListener("click", function (event) {
            event.preventDefault();

            const pathQueryParam
                = folderIcon.parentElement.querySelector('.relativePath').textContent.slice(0, -1);

            window.location.href = window.location.origin + window.location.pathname + '?path=' + encodeURIComponent(pathQueryParam);

        })



    });

    Array.from(fileIcons).forEach(function (fileIcon) {
        const objectName = fileIcon.parentElement.querySelector('p').textContent;
        const pathToObject = fileIcon.parentElement.querySelector('.relativePath').textContent;

        fileIcon.addEventListener("contextmenu", function (event) {
            event.preventDefault();

            customContextMenu.style.display = "block";
            customContextMenu.style.left = event.pageX + "px";
            customContextMenu.style.top = event.pageY + "px";

            const contextMenuDownload = document.getElementById('contextMenuDownload');
            contextMenuDownload.addEventListener("click", function (event) {
                document.getElementById('downloadFileForm').querySelector("[name='pathToFile']").value = pathToObject;
                document.getElementById('downloadFileForm').submit();
            })

            const contextMenuRename = document.getElementById('contextMenuRename');

            contextMenuRename.addEventListener('click', () => {
                document.getElementById('renameInput').value = objectName;
            });


            const contextMenuDelete = document.getElementById('contextMenuDelete');
            contextMenuDelete.addEventListener("click", function (event) {

                document.getElementById("deleteForm").querySelector("[name='objectName']").value = objectName;
                document.getElementById("deleteForm").querySelector("[name='currentPath']").value = currentPath;

                document.getElementById("deleteForm").submit();
            })

            const renameButton = document.getElementById('renameButton');
            const renameInput = document.getElementById('renameInput');
            const renameForm = document.getElementById('renameForm');

            renameButton.addEventListener("click", function() {
                const newName = renameInput.value;
                renameForm.querySelector("input[name='objectName']").value = newName;
                renameForm.querySelector("input[name='relativePathToObject']").value = pathToObject;
                renameForm.querySelector("input[name='currentPath']").value = currentPath;
                renameForm.submit();
            });


            document.addEventListener('click', hideContextMenu);

        });


    });

    const createFolderButton = document.getElementById('createFolderButton');
    createFolderButton.addEventListener('click', openCreateFolderModal);

    const createFolderSubmitButton = document.getElementById('createFolderSubmitButton');
    const createFolderInput = document.getElementById('createFolderInput');
    const createFolderForm = document.getElementById('createFolderForm');

    createFolderSubmitButton.addEventListener("click", function() {
        const newName = createFolderInput.value;
        createFolderForm.querySelector("input[name='objectName']").value = newName;
        createFolderForm.submit();
    });

    const uploadFileMenuItem = document.getElementById('uploadFileMenuItem');
    const fileInput = document.getElementById('fileInput');
    uploadFileMenuItem.addEventListener('click', () => fileInput.click());

    const uploadFileForm = document.getElementById('uploadFileForm');
    fileInput.addEventListener('change', (event) => {
        const fileList = event.target.files;
        if (fileList.length > 0) {
            const fileName = fileList[0].name;
            const objectNameField = uploadFileForm.querySelector('input[name="objectName"]');
            objectNameField.value = fileName;
        }
        uploadFileForm.submit();
    });

    const uploadFolderMenuItem = document.getElementById('uploadFolderMenuItem');
    const folderInput = document.getElementById('folderInput');
    uploadFolderMenuItem.addEventListener('click', () => folderInput.click());

    const uploadFolderForm = document.getElementById('uploadFolderForm');

    folderInput.addEventListener('change', (event) => {
        const fileList = event.target.files;
        if (fileList.length > 0) {
            const rootFolderName = fileList[0].webkitRelativePath.split('/')[0];
            const objectNameField = uploadFolderForm.querySelector('input[name="objectName"]');
            objectNameField.value = rootFolderName;
        }
        uploadFolderForm.submit();
    });


    const listOfValidationMessageDiv = document.getElementsByClassName("validation-message");
    if (listOfValidationMessageDiv.length > 0) {
        const firstElementInnerText = listOfValidationMessageDiv[0].innerText;
        const myToastEl = document.getElementById('toast');
        const myToast = new bootstrap.Toast(myToastEl);
        document.getElementById('toastBody').innerText = firstElementInnerText;
        myToast.show();

    }


});

function openCreateFolderModal() {
    const createFolderModal = new bootstrap.Modal(document.getElementById('createFolderModal'), {
        keyboard: false
    });
    createFolderModal.show();
}

function hideContextMenu() {
    customContextMenu.style.display = "none";
    document.removeEventListener("click", hideContextMenu);
}




