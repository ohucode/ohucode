var App = Vue.extend();

var router = new VueRouter({history: true});
var validator = window['vue-validator'];

Vue.use(validator);

var Root = Vue.extend({
    template: "루트 본문"    
});

var About = Vue.extend({
    template: "텍스트"    
});

router.map({
    '/': {
        component: Root
    },
    '/about': {
        component: About
    }
});

$(function() {
    //    router.start(App, "#app");
});

