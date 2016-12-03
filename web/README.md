# nextgisweb_tinymap
Simple Lefleat based map frontend for [NextGIS Web](http://nextgis.ru/nextgis-web).

One-page map with one layer added from NextGIS Web. You can click on features and see their attributes. This app provides an example of how to make maps with NGW for your website. It requiers nextgis.com premium account with CORS enabled.

Live demo: http://nextgis.github.io/nextgisweb_tinymap/

![screenshot](screenshot.png)


Features
--------------------

- [x] Icons for points
- [x] Identify window
- [x] Fields aliaces taken from NextGIS Web layer settings
- [x] Hide fields - taken from NextGIS Web layer settings
- [x] Feature name in identify window - taken from NextGIS Web layer settings
- [x] Showing photos from NextGIS Web
- [x] Attribution strings set in config file
- [x] Leaflet PointToLayer function store in separate mapstyle file
- [x] Links in attribution table making active automatically

Not implemented yet

- [ ] Feature description
- [ ] Multiple basemap set in config file
- [ ] Lines and polygons customizable styles
- [ ] Tested with points geometry
- [ ] Tested with line geometry
- [ ] Tested with polygon geometry
- [ ] NGW access error handling
- [ ] Code and dependencies license clean 



Installation
--------------------


 1. Rename config.example.js to config.js
 2. Set NGWLayerURL and attribution string in config.js

  ```
  NGWLayerURL: 'http://176.9.38.120/practice2/api/resource/31'
  ```

 3. Выставите права в NextGIS Web

Допустим, у нас в NGW такая структура: 

```
root
    L   Classifed data
    L   Open data
        L   somefolder 1
        L   somefolder 2
        L   data
            L   Your layer
```
```
Выставите в корне: Разрешить - Гость - Ресурс:Чтение - все ресурсы - нет
Выставите в группе следующей по уровню, то есть в Open data для гостя: Ресурс:Чтение, Структура данных:Чтение, Данные:Чтение. Метаданные:Чтение(?) - распространять.
```
 4.  Rename tinymap.mapstyle.example.js to tinymap.mapstyle.js You can customize vector map style here now.

Bounding box will be calculated authomatically.

using https://github.com/gregjacobs/Autolinker.js

License
-------------
This program is licensed under GNU GPL v2 or any later version

Commercial support
----------
Need to fix a bug or add a feature to nextgisweb_tinymap? We provide custom development and support for this software. [Contact us](http://nextgis.ru/en/contact/) to discuss options!

[![http://nextgis.com](http://nextgis.ru/img/nextgis.png)](http://nextgis.com)


