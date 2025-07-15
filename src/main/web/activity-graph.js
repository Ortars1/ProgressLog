// Функция для получения даты для ячейки
function getDateForCell(row, col, year) {
    const startDate = new Date(year, 0, 1); // 1 января выбранного года
    const dayOfWeek = startDate.getDay(); // День недели 1 января (0 - воскресенье, 1 - понедельник, и т.д.)
    const offset = (col * 7) + row - (dayOfWeek === 0 ? 6 : dayOfWeek - 1); // Смещение от 1 января
    const date = new Date(startDate);
    date.setDate(startDate.getDate() + offset);
    return date;
}

// Функция для построения таблицы
function buildTable(graphContainer, year) {
    const rows = 7;
    const cols = 53; // Максимальное количество недель в году
    const table = graphContainer.querySelector('.activity-graph');
    const monthsContainer = graphContainer.querySelector('.months');

    // Очищаем таблицу и контейнер месяцев
    table.innerHTML = '';
    monthsContainer.innerHTML = '';

    // Заполняем таблицу ячейками
    for (let row = 0; row < rows; row++) {
        const tr = document.createElement('tr');
        for (let col = 0; col < cols; col++) {
            const date = getDateForCell(row, col, year);
            const cell = document.createElement('td');

            if (date.getFullYear() === year) { // Только для выбранного года
                cell.setAttribute('data-date', date.toLocaleDateString('ru-RU'));
                cell.classList.add('has-date'); // Добавляем класс для ячеек с датой
            } else {
                cell.style.backgroundColor = 'transparent'; // Пустые ячейки для дней не из выбранного года
                cell.style.borderColor = 'transparent'; // Убираем рамку
                cell.style.cursor = 'default'; // Убираем курсор при наведении
            }

            tr.appendChild(cell);
        }
        table.appendChild(tr);
    }
    // Добавляем подписи месяцев
    const monthNames = ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'];
    let lastMonth = -1;

    for (let col = 0; col < cols; col++) {
        const date = getDateForCell(0, col, year);
        if (date.getFullYear() === year) {
            const month = date.getMonth();
            if (month !== lastMonth) {
                const monthElement = document.createElement('span');
                monthElement.textContent = monthNames[month];
                monthsContainer.appendChild(monthElement);
                lastMonth = month;
            } else {
                monthsContainer.appendChild(document.createElement('span'));
            }
        }
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    const graphContainers = document.querySelectorAll('.graph-container');

    graphContainers.forEach((graphContainer) => {
        const yearSelector = graphContainer.querySelector('.year-list');
        const initialYear = parseInt(yearSelector.value, 10);

        // Строим таблицу для текущего контейнера
        buildTable(graphContainer, initialYear);

        // Добавляем обработчик изменения выбранного года
        yearSelector.addEventListener('change', (event) => {
            const selectedYear = parseInt(event.target.value, 10);
            buildTable(graphContainer, selectedYear);
        });
    });
});

