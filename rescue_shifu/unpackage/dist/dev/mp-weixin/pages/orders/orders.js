"use strict";
const common_vendor = require("../../common/vendor.js");
const common_mockData = require("../../common/mockData.js");
const common_api = require("../../common/api.js");
const _sfc_defineComponent = common_vendor.defineComponent({
  __name: "orders",
  setup(__props) {
    const activeTab = common_vendor.ref("all");
    const refreshing = common_vendor.ref(false);
    const loading = common_vendor.ref(false);
    const orders = common_vendor.ref([]);
    const loginUserId = common_vendor.ref("");
    const syncLoginUserId = () => {
      const storedUserId = common_vendor.index.getStorageSync("master_user_id");
      loginUserId.value = storedUserId || "";
      return loginUserId.value;
    };
    const mapOrder = (item) => {
      var _a, _b;
      const raw = item.order;
      const dispatchStatus = raw.isDispatch === 1 ? raw.dispatchId ? "accepted" : "dispatched" : "none";
      const amountYuan = item.netPrice != null ? Number(item.netPrice) : raw.netPrice != null ? Number(raw.netPrice) : raw.price != null ? Number(raw.price) : 0;
      return new common_mockData.MyOrder({
        id: raw.id,
        orderNo: raw.orderNo || "-",
        createTime: raw.createTime ? new Date(raw.createTime).getTime() : Date.now(),
        serviceType: raw.serviceType || "-",
        plateNo: raw.plateNo || "-",
        status: raw.status ? raw.status == "COMPLETED" ? "done" : raw.status == "ACCEPTED" || raw.status == "DEPARTED" || raw.status == "ARRIVED" ? "processing" : "new" : "new",
        dispatchStatus,
        dispatchToName: raw.dispatchId || "",
        orderPrice: Number(amountYuan.toFixed(2)),
        ratio: (_a = raw.ratio) !== null && _a !== void 0 ? _a : null,
        remark: raw.remark || "",
        customerPhone: raw.customerPhone || "-",
        customerName: raw.customerName || "",
        address: raw.address || "",
        lat: raw.lat || 0,
        lng: raw.lng || 0,
        agencyId: raw.agencyId || "",
        agencyOrderType: (_b = raw.agencyOrderType) !== null && _b !== void 0 ? _b : null,
        masterId: raw.orderReceivingId || "",
        orderReceivingId: raw.orderReceivingId || "",
        images: [],
        feedbackImages: []
      });
    };
    const loadOrders = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        if (loading.value)
          return Promise.resolve(null);
        const operatorId = syncLoginUserId();
        if (!operatorId) {
          orders.value = [];
          refreshing.value = false;
          return Promise.resolve(null);
        }
        loading.value = true;
        try {
          common_vendor.index.__f__("log", "at pages/orders/orders.uvue:154", "[myOrders] fetching", new UTSJSONObject({ operatorId }));
          const data = yield common_api.getOrderList({ operatorId });
          common_vendor.index.__f__("log", "at pages/orders/orders.uvue:156", "[myOrders] raw data:", data);
          orders.value = data.map(mapOrder);
          common_vendor.index.__f__("log", "at pages/orders/orders.uvue:158", "[myOrders] fetched", new UTSJSONObject({ count: orders.value.length }));
        } catch (err) {
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "加载订单失败", icon: "none" });
        } finally {
          loading.value = false;
          refreshing.value = false;
        }
      });
    };
    const getLoginUserId = () => {
      return loginUserId.value;
    };
    const filteredOrders = common_vendor.computed(() => {
      const list = activeTab.value === "all" ? orders.value : orders.value.filter((o) => {
        return o.status === activeTab.value;
      });
      common_vendor.index.__f__("log", "at pages/orders/orders.uvue:176", "[myOrders] filter", new UTSJSONObject({
        activeTab: activeTab.value,
        total: orders.value.length,
        matched: list.length
      }));
      return list;
    });
    const switchTab = (tab) => {
      activeTab.value = tab;
    };
    const statusText = (s) => {
      if (s === "new")
        return "待接单";
      if (s === "processing")
        return "进行中";
      return "已完成";
    };
    const statusClass = (s) => {
      if (s === "new")
        return "st-new";
      if (s === "processing")
        return "st-processing";
      return "st-done";
    };
    const goDetail = (id) => {
      common_vendor.index.navigateTo({ url: `/pages/order-detail/order-detail?id=${id}` });
    };
    const callPhone = (phone) => {
      if (!phone || phone == "-")
        return null;
      common_vendor.index.makePhoneCall({ phoneNumber: phone });
    };
    const openMap = (item) => {
      if (item.lat == 0 || item.lng == 0)
        return null;
      common_vendor.index.openLocation({
        latitude: item.lat,
        longitude: item.lng,
        name: item.address,
        address: item.address
      });
    };
    const revokeDispatch = (id) => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        const operatorId = getLoginUserId();
        if (!operatorId) {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          return Promise.resolve(null);
        }
        common_vendor.index.showModal(new UTSJSONObject({
          title: "提示",
          content: "确定要撤回派单吗？",
          success: (res) => {
            return common_vendor.__awaiter(this, void 0, void 0, function* () {
              if (!res.confirm)
                return Promise.resolve(null);
              try {
                yield common_api.revokeDispatch(id, operatorId);
                common_vendor.index.showToast({ title: "撤回成功", icon: "success" });
                loadOrders();
              } catch (err) {
                common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "撤回失败", icon: "none" });
              }
            });
          }
        }));
      });
    };
    const currentDispatchOrderId = common_vendor.ref("");
    const currentDispatchToken = common_vendor.ref("");
    common_vendor.ref("");
    const showShareMask = common_vendor.ref(false);
    const setCommissionAndShare = (orderId) => {
      const order = UTS.arrayFind(orders.value, (o) => {
        return o.id === orderId;
      });
      if (order == null)
        return null;
      const operatorId = getLoginUserId();
      if (!operatorId) {
        common_vendor.index.showToast({ title: "请先登录", icon: "none" });
        return null;
      }
      if (order.agencyId !== operatorId) {
        common_vendor.index.showToast({ title: "仅服务商可派单", icon: "none" });
        return null;
      }
      if (order.status !== "processing") {
        common_vendor.index.showToast({ title: "当前订单不可派单", icon: "none" });
        return null;
      }
      common_vendor.index.showModal(new UTSJSONObject({
        title: "派单抽成金额(元)",
        editable: true,
        placeholderText: "请输入固定抽成金额",
        success: (res) => {
          return common_vendor.__awaiter(this, void 0, void 0, function* () {
            if (!res.confirm)
              return Promise.resolve(null);
            const input = (res.content || "").trim();
            if (!/^\d+(\.\d{1,2})?$/.test(input)) {
              common_vendor.index.showToast({ title: "请输入有效金额", icon: "none" });
              return Promise.resolve(null);
            }
            const amountYuan = Number(input);
            if (isNaN(amountYuan) || amountYuan < 0) {
              common_vendor.index.showToast({ title: "请输入有效金额", icon: "none" });
              return Promise.resolve(null);
            }
            const commissionAmount = Math.round(amountYuan * 100);
            try {
              const resp = yield common_api.createDispatch(orderId, operatorId, commissionAmount);
              currentDispatchOrderId.value = orderId;
              currentDispatchToken.value = resp.token;
              const idx = orders.value.findIndex((o) => {
                return o.id === orderId;
              });
              if (idx >= 0) {
                orders.value[idx].dispatchStatus = "dispatched";
                orders.value[idx].dispatchToName = "待师傅确认";
              }
              showShareMask.value = true;
            } catch (err) {
              common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "派单失败", icon: "none" });
            }
          });
        }
      }));
    };
    const onRefresh = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        refreshing.value = true;
        loadOrders();
      });
    };
    common_vendor.onShow(() => {
      syncLoginUserId();
      loadOrders();
    });
    common_vendor.onMounted(() => {
      common_vendor.index.$on("auth:login", () => {
        common_vendor.index.__f__("log", "at pages/orders/orders.uvue:317", "[myOrders] auth:login received, refreshing");
        syncLoginUserId();
        loadOrders();
      });
    });
    common_vendor.onUnmounted(() => {
      common_vendor.index.$off("auth:login");
    });
    common_vendor.onShareAppMessage((res = null) => {
      var _a, _b;
      const anyRes = res;
      const orderId = (_b = (_a = anyRes === null || anyRes === void 0 ? null : anyRes.target) === null || _a === void 0 ? null : _a.dataset) === null || _b === void 0 ? null : _b.orderId;
      let path = "/pages/orders/orders";
      if (orderId && currentDispatchOrderId.value === orderId && currentDispatchToken.value) {
        path = `/pages/order-detail/order-detail?dispatchToken=${currentDispatchToken.value}&from=dispatch`;
      }
      return new UTSJSONObject({
        title: "【派单】紧急救援订单，请尽快处理",
        path
      });
    });
    return (_ctx, _cache) => {
      "raw js";
      const __returned__ = common_vendor.e({
        a: common_vendor.unref(activeTab) === "all"
      }, common_vendor.unref(activeTab) === "all" ? {} : {}, {
        b: common_vendor.unref(activeTab) === "all" ? 1 : "",
        c: common_vendor.o(($event) => {
          return switchTab("all");
        }),
        d: common_vendor.unref(activeTab) === "processing"
      }, common_vendor.unref(activeTab) === "processing" ? {} : {}, {
        e: common_vendor.unref(activeTab) === "processing" ? 1 : "",
        f: common_vendor.o(($event) => {
          return switchTab("processing");
        }),
        g: common_vendor.unref(activeTab) === "done"
      }, common_vendor.unref(activeTab) === "done" ? {} : {}, {
        h: common_vendor.unref(activeTab) === "done" ? 1 : "",
        i: common_vendor.o(($event) => {
          return switchTab("done");
        }),
        j: common_vendor.unref(filteredOrders).length === 0
      }, common_vendor.unref(filteredOrders).length === 0 ? {} : {}, {
        k: common_vendor.f(common_vendor.unref(filteredOrders), (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.unref(showShareMask) && common_vendor.unref(currentDispatchOrderId) === item.id
          }, common_vendor.unref(showShareMask) && common_vendor.unref(currentDispatchOrderId) === item.id ? {
            b: item.id,
            c: common_vendor.o(($event) => {
              return showShareMask.value = false;
            }, item.id)
          } : {}, {
            d: common_vendor.t(statusText(item.status)),
            e: common_vendor.n(statusClass(item.status)),
            f: common_vendor.t(item.serviceType),
            g: common_vendor.t(item.orderNo),
            h: common_vendor.t(item.plateNo),
            i: common_vendor.t(item.remark || "无"),
            j: common_vendor.t(item.customerPhone),
            k: common_vendor.o(($event) => {
              return callPhone(item.customerPhone);
            }, item.id),
            l: common_vendor.t(item.address),
            m: common_vendor.o(($event) => {
              return openMap(item);
            }, item.id),
            n: common_vendor.t(item.orderPrice),
            o: common_vendor.o(($event) => {
              return goDetail(item.id);
            }, item.id),
            p: common_vendor.o(($event) => {
              return goDetail(item.id);
            }, item.id),
            q: item.status === "processing"
          }, item.status === "processing" ? common_vendor.e({
            r: item.agencyId === getLoginUserId()
          }, item.agencyId === getLoginUserId() ? common_vendor.e({
            s: item.dispatchStatus !== "dispatched" && item.dispatchStatus !== "accepted" && item.agencyOrderType === 1
          }, item.dispatchStatus !== "dispatched" && item.dispatchStatus !== "accepted" && item.agencyOrderType === 1 ? {
            t: common_vendor.o(($event) => {
              return setCommissionAndShare(item.id);
            }, item.id)
          } : {
            v: common_vendor.o(($event) => {
              return revokeDispatch(item.id);
            }, item.id)
          }) : {}) : {}, {
            w: item.dispatchStatus === "dispatched" || item.dispatchStatus === "accepted"
          }, item.dispatchStatus === "dispatched" || item.dispatchStatus === "accepted" ? {
            x: common_vendor.t(item.dispatchToName || "待师傅确认")
          } : {}, {
            y: item.id
          });
        }),
        l: common_vendor.unref(refreshing),
        m: common_vendor.o(onRefresh),
        n: common_vendor.sei(common_vendor.gei(_ctx, ""), "view")
      });
      return __returned__;
    };
  }
});
_sfc_defineComponent.__runtimeHooks = 2;
wx.createPage(_sfc_defineComponent);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/orders/orders.js.map
