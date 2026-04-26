let viewCounter = 1;

document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM загружен");
    console.log("listDataArray при загрузке:", window.listDataArray);
    
    setTimeout(function() {
        initializeCollapsibleView(0);
    }, 100);
    
    const addBtn = document.getElementById('addViewBtn');
    if (addBtn) {
        addBtn.addEventListener('click', addNewView);
        console.log("Обработчик Add view назначен");
    } else {
        console.error("Кнопка Add view не найдена!");
    }
});

function initializeCollapsibleView(viewId) {
    console.log("Инициализируем представление:", viewId);
    const view = document.querySelector('[data-view-id="' + viewId + '"]');
    
    if (!view) {
        console.error("View не найден для id:", viewId);
        return;
    }
    
    const toggles = view.querySelectorAll('.toggle');
    console.log("Найдено toggle элементов:", toggles.length);
    
    toggles.forEach(function(btn) {
        const idx = btn.getAttribute('data-idx');
        const sublistId = 'sublist-' + viewId + '-' + idx;
        const sublist = document.getElementById(sublistId);
        
        if (!sublist) {
            console.error("Sublist не найден для id:", sublistId);
            return;
        }
        
        sublist.style.display = 'none';
        btn.textContent = '[+]';
        btn.style.cursor = 'pointer';
        
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            if (sublist.style.display === 'none' || sublist.style.display === '') {
                sublist.style.display = 'block';
                btn.textContent = '[-]';
            } else {
                sublist.style.display = 'none';
                btn.textContent = '[+]';
            }
        });
    });
}

function addNewView() {
    console.log("Add view нажата");
    console.log("window.listDataArray в момент клика:", window.listDataArray);
    
    const container = document.getElementById('viewsContainer');
    const viewId = viewCounter++;
    
    const newView = document.createElement('div');
    newView.className = 'view';
    newView.setAttribute('data-view-id', viewId);
    
    let html = '<ol id="mainList-' + viewId + '">';
    
    if (window.listDataArray && Array.isArray(window.listDataArray) && window.listDataArray.length > 0) {
        console.log("Добавляем элементы из listDataArray");
        window.listDataArray.forEach(function(group, index) {
            console.log("Группа " + index + ":", group);
            const itemsStr = group.items.join(', ');
            html += '<li>' + group.name + ' (' + itemsStr + ')</li>';
        });
    } else {
        console.error("listDataArray недоступен, пуст или не является массивом:", window.listDataArray);
        html += '<li>Ошибка: данные не загружены</li>';
    }
    
    html += '</ol>';
    newView.innerHTML = html;
    container.appendChild(newView);
    
    console.log("Новое представление добавлено с id:", viewId);
}
