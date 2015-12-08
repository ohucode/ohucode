(function (scope, $) {
    var App = Vue.extend();

    var router = new VueRouter({history: true});
    var validator = scope['vue-validator'];

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

    //new Vue({el: "#app"});

    var ValidationRegex = {
        email: /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
        nickname: /\w{4,16}/
    };

    var VR = window. VR = ValidationRegex;

    var validation_class = function(value, regex) {
        console.log([value, regex]);
        var empty = value == "";
        var valid = regex.test(value);
        return {
            'has-error': !empty && !valid,
            'has-success': !empty && valid
        };
    };

    Vue.filter('feedback_class', function(value) {
        console.log(value);
        return {
            'has-success': 'glyphicon-ok',
            'has-error': 'glyphicon-remove'
        }[value];
    });

    new Vue({
        el: "#signup-form",
        data: {
            email: {
                value: "",
                pattern: VR.email
            },
            nickname: {
                value: "",
                pattern: VR.nickname
            }
        },
        computed: {
            class_email: function() {
                return validation_class(this.email, VR.email);
            },
            class_nickname: function() {
                return validation_class(this.nickname, VR.nickname);
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
})(window, jQuery);
