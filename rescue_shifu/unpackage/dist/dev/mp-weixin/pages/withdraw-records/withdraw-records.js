"use strict";
const common_vendor = require("../../common/vendor.js");
const common_api = require("../../common/api.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "withdraw-records",
  setup(__props) {
    const list = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const filterStatus = common_vendor.ref("");
    const normalizedStatus = (status = null) => {
      if (status === "SUCCESS")
        return "SUCCESS";
      if (status === "CLOSED" || status === "ABNORMAL")
        return "FAILED";
      return "APPLYING";
    };
    const statusText = (status = null) => {
      const s = normalizedStatus(status);
      if (s === "SUCCESS")
        return "提现成功";
      if (s === "FAILED")
        return "提现失败";
      return "处理中";
    };
    const statusClass = (status = null) => {
      const s = normalizedStatus(status);
      if (s === "SUCCESS")
        return "st-success";
      if (s === "FAILED")
        return "st-failed";
      return "st-applying";
    };
    const filteredList = common_vendor.computed(() => {
      if (!filterStatus.value)
        return list.value;
      return list.value.filter((it) => {
        return normalizedStatus(it.status) === filterStatus.value;
      });
    });
    const setFilter = (status) => {
      filterStatus.value = status;
    };
    const loadData = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        const masterId = common_vendor.index.getStorageSync("master_user_id");
        if (!masterId) {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          return Promise.resolve(null);
        }
        loading.value = true;
        try {
          const data = yield common_api.getWithdrawReconcile(masterId);
          list.value = Array.isArray(data.list) ? data.list : [];
        } catch (err) {
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "加载失败", icon: "none" });
        } finally {
          loading.value = false;
        }
      });
    };
    common_vendor.onShow(() => {
      loadData();
    });
    return (_ctx, _cache) => {
      "raw js";
      const __returned__ = common_vendor.e({
        a: common_vendor.n(common_vendor.unref(filterStatus) === "" ? "active" : ""),
        b: common_vendor.o(($event) => {
          return setFilter("");
        }),
        c: common_vendor.n(common_vendor.unref(filterStatus) === "APPLYING" ? "active" : ""),
        d: common_vendor.o(($event) => {
          return setFilter("APPLYING");
        }),
        e: common_vendor.n(common_vendor.unref(filterStatus) === "SUCCESS" ? "active" : ""),
        f: common_vendor.o(($event) => {
          return setFilter("SUCCESS");
        }),
        g: common_vendor.n(common_vendor.unref(filterStatus) === "FAILED" ? "active" : ""),
        h: common_vendor.o(($event) => {
          return setFilter("FAILED");
        }),
        i: common_vendor.unref(loading)
      }, common_vendor.unref(loading) ? {} : common_vendor.unref(filteredList).length === 0 ? {} : {
        k: common_vendor.f(common_vendor.unref(filteredList), (item, idx, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.applyNo || "-"),
            b: common_vendor.t(statusText(item.status)),
            c: common_vendor.n(statusClass(item.status)),
            d: common_vendor.t(Number(item.transferAmountYuan || 0).toFixed(2)),
            e: common_vendor.t(item.applyTime || "-"),
            f: item.failReason
          }, item.failReason ? {
            g: common_vendor.t(item.failReason)
          } : {}, {
            h: idx
          });
        })
      }, {
        j: common_vendor.unref(filteredList).length === 0,
        l: common_vendor.sei(common_vendor.gei(_ctx, ""), "view")
      });
      return __returned__;
    };
  }
});
wx.createPage(_sfc_main);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/withdraw-records/withdraw-records.js.map
