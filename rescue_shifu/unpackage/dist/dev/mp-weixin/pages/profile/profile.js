"use strict";
const common_vendor = require("../../common/vendor.js");
const common_api = require("../../common/api.js");
const STORAGE_KEY = "master_auth_demo";
const PROFILE_DRAFT_KEY = "master_profile_draft";
class AuthDemo extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          name: { type: String, optional: false },
          phone: { type: String, optional: false }
        };
      },
      name: "AuthDemo"
    };
  }
  constructor(options, metadata = AuthDemo.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.name = this.__props__.name;
    this.phone = this.__props__.phone;
    delete this.__props__;
  }
}
class ProfileDraft extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          nickname: { type: String, optional: true },
          avatarUrl: { type: String, optional: true }
        };
      },
      name: "ProfileDraft"
    };
  }
  constructor(options, metadata = ProfileDraft.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.nickname = this.__props__.nickname;
    this.avatarUrl = this.__props__.avatarUrl;
    delete this.__props__;
  }
}
class WxPhoneLoginResp extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          code: { type: Number, optional: true },
          message: { type: String, optional: true },
          data: { type: "Unknown", optional: true }
        };
      },
      name: "WxPhoneLoginResp"
    };
  }
  constructor(options, metadata = WxPhoneLoginResp.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.code = this.__props__.code;
    this.message = this.__props__.message;
    this.data = this.__props__.data;
    delete this.__props__;
  }
}
class WxPhoneLoginPayload extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          loginCode: { type: String, optional: false },
          phoneCode: { type: String, optional: false },
          clientType: { type: String, optional: false },
          nickName: { type: String, optional: false }
        };
      },
      name: "WxPhoneLoginPayload"
    };
  }
  constructor(options, metadata = WxPhoneLoginPayload.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.loginCode = this.__props__.loginCode;
    this.phoneCode = this.__props__.phoneCode;
    this.clientType = this.__props__.clientType;
    this.nickName = this.__props__.nickName;
    delete this.__props__;
  }
}
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "profile",
  setup(__props) {
    const avatarUrl = common_vendor.ref("/static/logo.png");
    const isLogin = common_vendor.ref(false);
    const masterName = common_vendor.ref("");
    const phone = common_vendor.ref("");
    const pendingNickname = common_vendor.ref("");
    common_vendor.ref(false);
    const balance = common_vendor.ref(0);
    const freezeBalance = common_vendor.ref(0);
    const reconcileList = common_vendor.ref([]);
    const loadingWallet = common_vendor.ref(false);
    const loadingWithdraw = common_vendor.ref(false);
    const loadingLogin = common_vendor.ref(false);
    const agreeProtocol = common_vendor.ref(false);
    const loadProfileDraft = () => {
      const raw = common_vendor.index.getStorageSync(PROFILE_DRAFT_KEY);
      if (!raw) {
        return null;
      }
      try {
        const draft = UTS.JSON.parse(raw);
        if (draft.nickname) {
          pendingNickname.value = draft.nickname;
        }
        if (draft.avatarUrl) {
          avatarUrl.value = draft.avatarUrl;
        }
      } catch (e) {
        common_vendor.index.removeStorageSync(PROFILE_DRAFT_KEY);
      }
    };
    const saveProfileDraft = () => {
      const draft = new UTSJSONObject({
        nickname: pendingNickname.value || "",
        avatarUrl: avatarUrl.value || ""
      });
      common_vendor.index.setStorageSync(PROFILE_DRAFT_KEY, UTS.JSON.stringify(draft));
    };
    const loadAuth = () => {
      const raw = common_vendor.index.getStorageSync(STORAGE_KEY);
      if (!raw) {
        isLogin.value = false;
        masterName.value = "";
        phone.value = "";
        loadProfileDraft();
        return null;
      }
      try {
        const data = UTS.JSON.parse(raw);
        isLogin.value = true;
        masterName.value = data.name;
        pendingNickname.value = data.name;
        phone.value = data.phone;
        saveProfileDraft();
      } catch (e) {
        common_vendor.index.removeStorageSync(STORAGE_KEY);
        isLogin.value = false;
        loadProfileDraft();
      }
    };
    const fetchWallet = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        var _a, _b;
        if (!isLogin.value) {
          return Promise.resolve(null);
        }
        if (loadingWallet.value) {
          return Promise.resolve(null);
        }
        const masterId = common_vendor.index.getStorageSync("master_user_id");
        if (!masterId) {
          return Promise.resolve(null);
        }
        loadingWallet.value = true;
        try {
          const data = yield common_api.getWithdrawReconcile(masterId);
          balance.value = Number((_a = data.availableBalanceYuan) !== null && _a !== void 0 ? _a : 0);
          freezeBalance.value = Number((_b = data.freezeBalanceYuan) !== null && _b !== void 0 ? _b : 0);
          reconcileList.value = Array.isArray(data.list) ? data.list : [];
        } catch (err) {
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "获取钱包失败", icon: "none" });
        } finally {
          loadingWallet.value = false;
        }
      });
    };
    const balanceText = common_vendor.computed(() => {
      return balance.value.toFixed(2);
    });
    const canWithdraw = common_vendor.computed(() => {
      return isLogin.value && balance.value >= 10 && !loadingWithdraw.value;
    });
    const withdrawButtonText = common_vendor.computed(() => {
      if (!isLogin.value) {
        return "未登录不可提现";
      }
      if (loadingWithdraw.value) {
        return "处理中...";
      }
      if (balance.value < 10) {
        return "满10元可提现";
      }
      return "立即提现";
    });
    common_vendor.onShow(() => {
      loadAuth();
      if (isLogin.value) {
        fetchWallet();
      }
    });
    common_vendor.onMounted(() => {
      common_vendor.index.$on("wallet:refresh", () => {
        if (isLogin.value) {
          fetchWallet();
        }
      });
    });
    common_vendor.onUnmounted(() => {
      common_vendor.index.$off("wallet:refresh");
    });
    const requestWxPhoneLogin = (payload) => {
      return new Promise((resolve, reject) => {
        if (!common_api.LOGIN_URL) {
          reject(new Error("请先在 pages/profile/profile.uvue 中配置 LOGIN_URL"));
          return null;
        }
        common_vendor.index.request({
          url: common_api.LOGIN_URL,
          method: "POST",
          data: payload,
          success: (res) => {
            resolve(res.data);
          },
          fail: (err) => {
            reject(err);
          }
        });
      });
    };
    const handleAgreePrivacyAuthorization = () => {
      common_vendor.index.__f__("log", "at pages/profile/profile.uvue:316", "[privacy] user agreed privacy authorization");
    };
    const handleAgreeChange = (e = null) => {
      agreeProtocol.value = e.detail.value.includes("agree");
    };
    const handleGetPhoneNumber = (e = null) => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        if (loadingLogin.value) {
          return Promise.resolve(null);
        }
        loadingLogin.value = true;
        common_vendor.index.showLoading({ title: "登录中..." });
        try {
          const detail = e === null || e === void 0 ? null : e.detail;
          const phoneCode = (detail === null || detail === void 0 ? null : detail.code) ? String(detail.code) : "";
          if (!phoneCode) {
            const msg = detail && detail.errMsg ? String(detail.errMsg) : "未获取到手机号授权码";
            common_vendor.index.showToast({ title: msg, icon: "none" });
            return Promise.resolve(null);
          }
          const loginRes = yield common_vendor.index.login(new UTSJSONObject({ provider: "weixin" }));
          const loginCode = loginRes.code;
          if (!loginCode) {
            throw new Error("wx.login 未获取到 loginCode");
          }
          const resp = yield requestWxPhoneLogin(new WxPhoneLoginPayload({
            loginCode,
            phoneCode,
            clientType: "shifu",
            nickName: "微信用户"
          }));
          if (resp.code === 200 && resp.data) {
            const loginData = resp.data;
            common_vendor.index.setStorageSync("master_token", loginData.token);
            if (loginData.userId) {
              common_vendor.index.setStorageSync("master_user_id", loginData.userId);
            }
            const finalName = loginData.nickName || "用户";
            const finalPhone = loginData.phoneNumber || "";
            const authData = new AuthDemo({
              name: finalName,
              phone: finalPhone
            });
            common_vendor.index.setStorageSync(STORAGE_KEY, UTS.JSON.stringify(authData));
            masterName.value = finalName;
            phone.value = finalPhone;
            isLogin.value = true;
            pendingNickname.value = finalName;
            saveProfileDraft();
            fetchWallet();
            common_vendor.index.__f__("log", "at pages/profile/profile.uvue:372", "[auth] login success, userId:", loginData.userId || "", "nick:", finalName);
            common_vendor.index.$emit("auth:login", new UTSJSONObject({ userId: loginData.userId || "", name: finalName }));
            common_vendor.index.showToast({ title: "登录成功", icon: "success" });
          } else {
            common_vendor.index.showToast({ title: resp.message || "登录失败", icon: "none" });
          }
        } catch (err) {
          let errText = "";
          try {
            errText = UTS.JSON.stringify(err);
          } catch (e2) {
            errText = String(err);
          }
          common_vendor.index.__f__("error", "at pages/profile/profile.uvue:385", "登录异常:", err, errText);
          const msg = (err === null || err === void 0 ? null : err.message) ? String(err.message) : errText || "网络异常";
          common_vendor.index.showToast({ title: msg, icon: "none" });
        } finally {
          common_vendor.index.hideLoading();
          loadingLogin.value = false;
        }
      });
    };
    const logout = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        common_vendor.index.showModal(new UTSJSONObject({
          title: "退出登录",
          content: "确认退出当前账号？",
          success: (res) => {
            if (res.confirm) {
              common_vendor.index.clearStorageSync();
              loadAuth();
              balance.value = 0;
              common_vendor.index.showToast({ title: "已退出", icon: "success" });
            }
          }
        }));
      });
    };
    const parseAmount = (val) => {
      if (!/^\d+(\.\d{1,2})?$/.test(val))
        return 0;
      const num = Number(val);
      if (isNaN(num))
        return 0;
      return num;
    };
    const handleWithdrawClick = () => {
      if (!canWithdraw.value) {
        return null;
      }
      const current = balance.value;
      common_vendor.index.showModal(new UTSJSONObject({
        title: "提现申请",
        editable: true,
        placeholderText: `请输入提现金额（可提￥${current.toFixed(2)}）`,
        success: (res) => {
          if (!res.confirm) {
            return null;
          }
          const amount = parseAmount(String(res.content || "").trim());
          if (amount <= 0) {
            common_vendor.index.showToast({ title: "请输入正确金额", icon: "none" });
            return null;
          }
          if (amount > balance.value) {
            common_vendor.index.showToast({ title: "提现金额不能大于余额", icon: "none" });
            return null;
          }
          requestWithdraw(amount);
        }
      }));
    };
    const requestWithdraw = (amount) => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        if (loadingWithdraw.value) {
          return Promise.resolve(null);
        }
        const masterId = common_vendor.index.getStorageSync("master_user_id");
        if (!masterId) {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          return Promise.resolve(null);
        }
        loadingWithdraw.value = true;
        try {
          const requestId = `${masterId}-${Date.now()}`;
          const created = yield common_api.createWithdraw(new common_api.WithdrawCreateParams({
            masterId,
            amountYuan: amount,
            accountId: masterId,
            requestId
          }));
          if (!created.applyNo) {
            throw new Error("提现单创建失败，请稍后重试");
          }
          yield common_api.applyWithdraw(new common_api.WithdrawApplyParams({
            applyNo: created.applyNo
          }));
          common_vendor.index.showToast({ title: "提现申请已提交", icon: "success" });
          setTimeout(() => {
            common_vendor.index.showToast({ title: "处理中，请在记录中查看进度", icon: "none" });
          }, 400);
          yield fetchWallet();
        } catch (err) {
          const msg = (err === null || err === void 0 ? null : err.message) ? String(err.message) : "提现申请失败";
          common_vendor.index.showToast({ title: msg, icon: "none" });
        } finally {
          loadingWithdraw.value = false;
        }
      });
    };
    return (_ctx, _cache) => {
      "raw js";
      const __returned__ = common_vendor.e({
        a: common_vendor.unref(avatarUrl),
        b: common_vendor.t(common_vendor.unref(isLogin) ? common_vendor.unref(masterName) : "未登录"),
        c: common_vendor.t(common_vendor.unref(isLogin) ? common_vendor.unref(phone) : "请先登录以接单"),
        d: !common_vendor.unref(isLogin)
      }, !common_vendor.unref(isLogin) ? {
        e: common_vendor.unref(agreeProtocol),
        f: common_vendor.o(handleAgreeChange),
        g: common_vendor.t(common_vendor.unref(loadingLogin) ? "登录中..." : "手机号一键登录"),
        h: common_vendor.unref(loadingLogin) || !common_vendor.unref(agreeProtocol),
        i: common_vendor.o(handleGetPhoneNumber),
        j: common_vendor.o(handleAgreePrivacyAuthorization)
      } : {
        k: common_vendor.o(logout)
      }, {
        l: common_vendor.t(common_vendor.unref(isLogin) ? common_vendor.unref(balanceText) : "--"),
        m: common_vendor.t(common_vendor.unref(isLogin) ? "可用余额" : "登录后可查看钱包余额"),
        n: common_vendor.unref(isLogin)
      }, common_vendor.unref(isLogin) ? {
        o: common_vendor.t(common_vendor.unref(freezeBalance).toFixed(2))
      } : {}, {
        p: common_vendor.t(common_vendor.unref(withdrawButtonText)),
        q: common_vendor.n(common_vendor.unref(canWithdraw) ? "btn-primary" : "btn-disabled"),
        r: !common_vendor.unref(canWithdraw) || common_vendor.unref(loadingWithdraw),
        s: common_vendor.o(handleWithdrawClick),
        t: common_vendor.unref(isLogin) && common_vendor.unref(reconcileList).length > 0
      }, common_vendor.unref(isLogin) && common_vendor.unref(reconcileList).length > 0 ? {
        v: common_vendor.f(common_vendor.unref(reconcileList).slice(0, 5), (item, idx, i0) => {
          return {
            a: common_vendor.t(item.applyNo || item.applyTime || "提现记录"),
            b: common_vendor.t("-" + Number(item.transferAmountYuan || 0).toFixed(2) + " " + (item.status || "")),
            c: idx
          };
        })
      } : {}, {
        w: common_vendor.sei(common_vendor.gei(_ctx, ""), "view")
      });
      return __returned__;
    };
  }
});
wx.createPage(_sfc_main);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/profile.js.map
