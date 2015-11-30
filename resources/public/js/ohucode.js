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
    router.start(App, "#app");
});


//new Vue({el: "#app"});

var ValidationRegex = {
    email: /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
    nickname: /\w{4,16}/
};

var VR = ValidationRegex;

var validation_class = function(value, valid) {
    var empty = value == "";
    return {
        'has-error': !empty && !valid,
        'has-success': !empty && valid
    };
};


Vue.filter('validate_class', function (value, valid) {
    console.log([value, valid]);
    return 'has-error';
});

new Vue({
    el: "#sign-up-form",
    data: {
        email: "",
        nickname: ""
    },
    computed: {
        email_class: function() {
            return validation_class(this.email, this.valid_email());
        },
        nickname_class: function() {
            return validation_class(this.nickname, this.valid_nickname());
        },
        valid_email: function() {
            return VR.email.test(this.email);
        },
        valid_nickname: function() {
            return VR.nickname.test(this.nickname);
        },
        valid_form: function() {
            return this.valid_email && this.valid_nickname;
        }
    }
});
