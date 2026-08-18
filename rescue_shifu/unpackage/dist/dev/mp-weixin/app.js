"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
if (!Math) {
  "./pages/index/index.js";
  "./pages/orders/orders.js";
  "./pages/profile/profile.js";
  "./pages/order-detail/order-detail.js";
  "./pages/withdraw-records/withdraw-records.js";
  "./pages/settings/settings.js";
}
const AUTH_WHITELIST = ["/pages/profile/profile"];
const normalizeUrl = (url) => {
  const idx = url.indexOf("?");
  return idx >= 0 ? url.slice(0, idx) : url;
};
const isAuthed = () => {
  const token = common_vendor.index.getStorageSync("master_token");
  return Boolean(token);
};
const isInWhitelist = (url) => {
  const path = normalizeUrl(url);
  return AUTH_WHITELIST.includes(path);
};
const redirectToLogin = () => {
  common_vendor.index.switchTab({ url: "/pages/profile/profile" });
};
const guardRoute = (url) => {
  if (isInWhitelist(url)) {
    return true;
  }
  if (isAuthed()) {
    return true;
  }
  redirectToLogin();
  return false;
};
const _sfc_main = common_vendor.defineComponent({
  onLaunch() {
    common_vendor.index.__f__("log", "at App.uvue:40", "App Launch");
    common_vendor.index.addInterceptor("navigateTo", new UTSJSONObject({
      invoke: (args = null) => {
        return guardRoute(args.url);
      }
    }));
    common_vendor.index.addInterceptor("redirectTo", new UTSJSONObject({
      invoke: (args = null) => {
        return guardRoute(args.url);
      }
    }));
    common_vendor.index.addInterceptor("reLaunch", new UTSJSONObject({
      invoke: (args = null) => {
        return guardRoute(args.url);
      }
    }));
    common_vendor.index.addInterceptor("switchTab", new UTSJSONObject({
      invoke: (args = null) => {
        return guardRoute(args.url);
      }
    }));
  },
  onShow() {
    common_vendor.index.__f__("log", "at App.uvue:55", "App Show");
    const pages = getCurrentPages();
    const current = pages.length > 0 ? "/" + pages[pages.length - 1].route : "";
    if (current && !isInWhitelist(current) && !isAuthed()) {
      redirectToLogin();
    }
  },
  onHide() {
    common_vendor.index.__f__("log", "at App.uvue:63", "App Hide");
  },
  onExit() {
    common_vendor.index.__f__("log", "at App.uvue:84", "App Exit");
  }
});
function createApp() {
  const app = common_vendor.createSSRApp(_sfc_main);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
//# sourceMappingURL=../.sourcemap/mp-weixin/app.js.map
