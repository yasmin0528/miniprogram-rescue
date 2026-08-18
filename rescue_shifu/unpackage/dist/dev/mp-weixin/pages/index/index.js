"use strict";
const common_vendor = require("../../common/vendor.js");
const common_mockData = require("../../common/mockData.js");
const common_api = require("../../common/api.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const refreshing = common_vendor.ref(false);
    const loadingOrders = common_vendor.ref(false);
    const orders = common_vendor.ref([]);
    const currentLocation = common_vendor.ref(null);
    const mapHallOrder = (item) => {
      var _a;
      const raw = item.order;
      const amountYuan = item.netPrice != null ? Number(item.netPrice) : raw.netPrice != null ? Number(raw.netPrice) : raw.price != null ? Number(raw.price) : 0;
      return new common_mockData.MyOrder({
        id: raw.id,
        orderNo: raw.orderNo || "-",
        createTime: raw.createTime ? new Date(raw.createTime).getTime() : Date.now(),
        serviceType: raw.serviceType || "-",
        plateNo: raw.plateNo || "-",
        status: "new",
        dispatchStatus: "none",
        dispatchToName: "",
        orderPrice: Number(amountYuan.toFixed(2)),
        ratio: (_a = raw.ratio) !== null && _a !== void 0 ? _a : null,
        remark: raw.remark || "",
        customerPhone: raw.customerPhone || "-",
        customerName: raw.customerName || "",
        address: raw.address || "",
        lat: raw.lat || 0,
        lng: raw.lng || 0,
        agencyId: raw.agencyId || "",
        masterId: "",
        orderReceivingId: raw.orderReceivingId || "",
        images: [],
        feedbackImages: []
      });
    };
    const loadOrders = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        if (loadingOrders.value)
          return Promise.resolve(null);
        const loc = currentLocation.value;
        if (!loc) {
          refreshing.value = false;
          return Promise.resolve(null);
        }
        const storedUserId = common_vendor.index.getStorageSync("master_user_id");
        const userId = storedUserId || common_mockData.currentUserId.value;
        if (!userId) {
          orders.value = [];
          refreshing.value = false;
          return Promise.resolve(null);
        }
        loadingOrders.value = true;
        try {
          const data = yield common_api.getShifuHallOrders({
            userId,
            lat: loc.lat,
            lng: loc.lng,
            radiusKm: 10,
            visibleStatuses: "PAID",
            excludeStatus: "CREATED,ACCEPTED,DEPARTED,ARRIVED,COMPLETED"
          });
          common_vendor.index.__f__("log", "at pages/index/index.uvue:127", "[hallOrders] raw data:", data);
          orders.value = data.map(mapHallOrder);
          common_vendor.index.__f__("log", "at pages/index/index.uvue:129", "[hallOrders] mapped orders:", orders.value);
        } catch (err) {
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "订单加载失败", icon: "none" });
        } finally {
          loadingOrders.value = false;
          refreshing.value = false;
        }
      });
    };
    const goDetail = (id) => {
      common_vendor.index.navigateTo({ url: `/pages/order-detail/order-detail?id=${id}` });
    };
    const formatTime = (ts) => {
      const d = new Date(ts);
      const pad = (n) => {
        return n < 10 ? "0" + n : "" + n;
      };
      return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
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
    const getTagClass = (type) => {
      switch (type) {
        case "搭电":
          return "tag-electricity";
        case "换胎":
          return "tag-tire";
        case "新能源紧急充电":
          return "tag-ev";
        default:
          return "tag-default";
      }
    };
    const onGrabOrder = (id) => {
      common_vendor.index.showModal(new UTSJSONObject({
        title: "提示",
        content: "确定要接下此订单吗？",
        success: (res) => {
          if (res.confirm) {
            doAcceptOrder(id);
          }
        }
      }));
    };
    const doAcceptOrder = (orderId) => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        try {
          const storedUserId = common_vendor.index.getStorageSync("master_user_id");
          const operatorId = storedUserId || common_mockData.currentUserId.value;
          if (!operatorId) {
            common_vendor.index.showToast({ title: "请先登录", icon: "none" });
            return Promise.resolve(null);
          }
          yield common_api.acceptOrder({ orderId, operatorId });
          common_vendor.index.showToast({ title: "接单成功", icon: "success" });
          common_vendor.index.switchTab({ url: "/pages/orders/orders" });
        } catch (err) {
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "接单失败", icon: "none" });
        }
      });
    };
    const updateCurrentLocation = () => {
      common_vendor.index.authorize(new UTSJSONObject({
        scope: "scope.userLocation",
        success: () => {
          common_vendor.index.getLocation(new UTSJSONObject({
            type: "gcj02",
            success: (res) => {
              currentLocation.value = { lat: res.latitude, lng: res.longitude };
              common_vendor.index.__f__("log", "at pages/index/index.uvue:208", "当前定位:", currentLocation.value);
              loadOrders();
            },
            fail: () => {
              currentLocation.value = null;
              orders.value = [];
              common_vendor.index.showToast({ title: "定位失败，请检查定位权限", icon: "none" });
            }
          }));
        },
        fail: () => {
          currentLocation.value = null;
          common_vendor.index.showModal(new UTSJSONObject({
            title: "需要定位权限",
            content: "请允许定位权限以查看附近 10km 订单",
            confirmText: "去开启",
            cancelText: "取消",
            success: (res) => {
              if (res.confirm) {
                common_vendor.index.openSetting();
              }
            }
          }));
        }
      }));
    };
    common_vendor.onShow(() => {
      const storedUserId = common_vendor.index.getStorageSync("master_user_id");
      if (!storedUserId && !common_mockData.currentUserId.value) {
        orders.value = [];
        return null;
      }
      updateCurrentLocation();
    });
    common_vendor.onMounted(() => {
      common_vendor.index.$on("auth:login", () => {
        common_vendor.index.__f__("log", "at pages/index/index.uvue:246", "[hallOrders] auth:login received, refreshing");
        updateCurrentLocation();
      });
    });
    common_vendor.onUnmounted(() => {
      common_vendor.index.$off("auth:login");
    });
    const onRefresh = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        refreshing.value = true;
        updateCurrentLocation();
      });
    };
    return (_ctx, _cache) => {
      "raw js";
      const __returned__ = common_vendor.e({
        a: common_vendor.unref(orders).length === 0
      }, common_vendor.unref(orders).length === 0 ? {} : {}, {
        b: common_vendor.f(common_vendor.unref(orders), (item, k0, i0) => {
          return {
            a: common_vendor.t(formatTime(item.createTime)),
            b: common_vendor.t(item.orderPrice),
            c: common_vendor.t(item.orderNo),
            d: common_vendor.t(item.plateNo),
            e: common_vendor.t(item.remark || "无"),
            f: common_vendor.t(item.customerPhone),
            g: common_vendor.o(($event) => {
              return callPhone(item.customerPhone);
            }, item.id),
            h: common_vendor.t(item.address),
            i: common_vendor.o(($event) => {
              return openMap(item);
            }, item.id),
            j: common_vendor.t(item.serviceType),
            k: common_vendor.n(getTagClass(item.serviceType)),
            l: common_vendor.o(($event) => {
              return goDetail(item.id);
            }, item.id),
            m: common_vendor.o(($event) => {
              return onGrabOrder(item.id);
            }, item.id),
            n: item.id,
            o: common_vendor.o(($event) => {
              return goDetail(item.id);
            }, item.id)
          };
        }),
        c: common_vendor.unref(refreshing),
        d: common_vendor.o(onRefresh),
        e: common_vendor.sei(common_vendor.gei(_ctx, ""), "view")
      });
      return __returned__;
    };
  }
});
wx.createPage(_sfc_main);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/index/index.js.map
