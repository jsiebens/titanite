/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function initialize() {

    var options = {
        zoom: 2,
        center: new google.maps.LatLng(0, 0),
    };

    var map = new google.maps.Map(document.getElementById("map_canvas"), options);

    if (!!window.EventSource) {
        var source = new EventSource('/data');

        source.addEventListener(
            'message',
            function(e) {
                var data = JSON.parse(e.data);
                var position = new google.maps.LatLng(data.lat,data.lng);
                var marker = new google.maps.Marker({position: position, map: map, icon: "tweet.png"});

                setTimeout(function() { marker.setMap(null); }, 500);
            },
            false
        );

    } else {
        document.getElementById("map_canvas").innerHTML = "Sorry. This browser doesn't seem to support Server sent event. Check <a href='http://html5test.com/compare/feature/communication-eventSource.html'>html5test</a> for browser compatibility.";
    }

}