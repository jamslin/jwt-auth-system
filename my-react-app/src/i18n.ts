import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

// Translation resources
const resources = {
  en: {
    translation: {
      // English translations
      welcome: "Welcome",
      login: "Login",
      register: "Register",
      email: "Email",
      password: "Password",
      home: "Home",
      about: "About",
      contact: "Contact",
      submit: "Submit",
      cancel: "Cancel"
    }
  },
  tc: {
    translation: {
      // Traditional Chinese translations
      welcome: "歡迎",
      login: "登入",
      register: "註冊",
      email: "電郵",
      password: "密碼",
      home: "主頁",
      about: "關於我們",
      contact: "聯絡我們",
      submit: "提交",
      cancel: "取消"
    }
  },
  sc: {
    translation: {
      // Simplified Chinese translations
      welcome: "欢迎",
      login: "登录",
      register: "注册",
      email: "邮箱",
      password: "密码",
      home: "主页",
      about: "关于我们",
      contact: "联系我们",
      submit: "提交",
      cancel: "取消"
    }
  }
};

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: 'en', // default language
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false
    }
  });

export default i18n;