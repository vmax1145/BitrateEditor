# BitrateEditor
Bitrate Editor for Ambarella based camers.
### !!! USE AT YOUR OWN RISK !!!

### Author is not resposible for any loss or damages caused by using this software.

### ИСПОЛЬЗУЕТЕ НА СВОЙ СТРАХ И РИСК. НИ АВТОР НИ КТО ИНОЙ НИ КАКОЙ ОТВЕТСТВЕННОСТИ НЕ НЕСЕТ

## Как пользоваться
Пока ТОЛЬКО для SJ8Pro прошивки на базе v1.3.0

1. Кладете содержимое архива в одну директорию с  SJ8_FWUPDATE.bin и SJ8_CHECK.ch
2. Запускаете bitrates.bat
3. Правите битрейты 
4. Сохраняете...  на выходе получите пару файлов SJ8_FWUPDATE.bin.mod и SJ8_CHECK.ch.mod

Строчки битрейтов (соответствие реальным режимам) могут содержать ошибки и не соответствовать реальным
просьба о замеченных несоответствиях сообщать.


## Для продвинутых
Программа теоретически адаптируется и для других версий прошивки и возможно даже для других аппаратов 
нужно только переписать конфиг..

Все адреса в конфиге в ДЕСЯТИЧНОМ виде.

_md5fileName_ опционален. при его наличии проверяет при старте мд5 и генерит новый при сохранении . Для прошивок где это не нужно можно строчку удалить из конфига.

_verify_ секция для проверки соответствия подсунутой прошивке конфигу. 
содержит произвольное число проверок типа адрес - строка  достаточных для верификации

_videoModes_ набор видеорежимов по одному на каждую тройку строк битрейтов в прошивке
inUse - просто пометка используется или нет
Формат названия 3840x2160 30P фиксированный !!! {ширина}х{высота} {фпс}{P|I}  
есть задумка его парсить для автоматизированой калькуляции так что соблюдайте..
Не распарсенные не будут участвовать в перекалькуляции... Но это пока задумка.

_адреса_
    "sectionStartAddr": 560,    (по началу заголовка секции)
    "sectionLen": 25710016,   (включая заголовок секции длинной 0x100)
    "sectionCrcAddr": 560,       (адрес в заголовке секции где лежит crc32 тела секции)
    "bitratesTableAddress": 25333104, (адрес начальной строки таблицы битрейтов) 
Все адреса это адрес во всей прошивке целиком 

Правила валидации при вводе значений пользователем
```javascript
"validate": {
     "bitrate": {"min":1, "max":120},
     "min": {"min":0.75, "max":1.0},
     "max": {"min":1.0, "max":1.25}
  }
 ```

