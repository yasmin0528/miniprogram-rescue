"use strict";
const common_vendor = require("../../common/vendor.js");
const common_mockData = require("../../common/mockData.js");
const common_api = require("../../common/api.js");
class MapMarker extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          id: { type: Number, optional: false },
          latitude: { type: Number, optional: false },
          longitude: { type: Number, optional: false },
          title: { type: String, optional: false },
          iconPath: { type: String, optional: true },
          width: { type: Number, optional: true },
          height: { type: Number, optional: true }
        };
      },
      name: "MapMarker"
    };
  }
  constructor(options, metadata = MapMarker.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.id = this.__props__.id;
    this.latitude = this.__props__.latitude;
    this.longitude = this.__props__.longitude;
    this.title = this.__props__.title;
    this.iconPath = this.__props__.iconPath;
    this.width = this.__props__.width;
    this.height = this.__props__.height;
    delete this.__props__;
  }
}
class MapPolylinePoint extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          latitude: { type: Number, optional: false },
          longitude: { type: Number, optional: false }
        };
      },
      name: "MapPolylinePoint"
    };
  }
  constructor(options, metadata = MapPolylinePoint.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.latitude = this.__props__.latitude;
    this.longitude = this.__props__.longitude;
    delete this.__props__;
  }
}
class MapPolyline extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          points: { type: UTS.UTSType.withGenerics(Array, [MapPolylinePoint]), optional: false },
          color: { type: String, optional: false },
          width: { type: Number, optional: false },
          dottedLine: { type: Boolean, optional: true }
        };
      },
      name: "MapPolyline"
    };
  }
  constructor(options, metadata = MapPolyline.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.points = this.__props__.points;
    this.color = this.__props__.color;
    this.width = this.__props__.width;
    this.dottedLine = this.__props__.dottedLine;
    delete this.__props__;
  }
}
class ReverseGeocodeResponse extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          result: { type: "Unknown", optional: true }
        };
      },
      name: "ReverseGeocodeResponse"
    };
  }
  constructor(options, metadata = ReverseGeocodeResponse.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.result = this.__props__.result;
    delete this.__props__;
  }
}
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "order-detail",
  setup(__props) {
    const id = common_vendor.ref("");
    const sharerId = common_vendor.ref("");
    const dispatchRatio = common_vendor.ref(null);
    const dispatchToken = common_vendor.ref("");
    const isFromDispatch = common_vendor.ref(false);
    const masterLat = common_vendor.ref(0);
    const masterLng = common_vendor.ref(0);
    const customerAddress = common_vendor.ref("");
    const masterAddress = common_vendor.ref("");
    const netPrice = common_vendor.ref(null);
    const dispatchMasterIncomeAmount = common_vendor.ref(null);
    const detail = common_vendor.ref(new common_mockData.MyOrder({
      id: "",
      orderNo: "-",
      createTime: Date.now(),
      serviceType: "-",
      remark: "",
      plateNo: "-",
      customerName: "-",
      customerPhone: "-",
      address: "-",
      orderPrice: 0,
      ratio: null,
      lat: 0,
      lng: 0,
      status: "new",
      dispatchStatus: "none",
      dispatchToName: "",
      agencyId: "",
      masterId: "",
      orderReceivingId: "",
      images: [],
      feedbackImages: [],
      serverStatus: ""
    }));
    const isProvider = common_vendor.computed(() => {
      return detail.value.agencyId == common_mockData.currentUserId.value;
    });
    const isMaster = common_vendor.computed(() => {
      return detail.value.masterId == common_mockData.currentUserId.value;
    });
    const mapCenterLat = common_vendor.computed(() => {
      if (detail.value.lat != 0)
        return detail.value.lat;
      if (masterLat.value != 0)
        return masterLat.value;
      return 0;
    });
    const mapCenterLng = common_vendor.computed(() => {
      if (detail.value.lng != 0)
        return detail.value.lng;
      if (masterLng.value != 0)
        return masterLng.value;
      return 0;
    });
    const displayAmount = common_vendor.computed(() => {
      var _a;
      if (dispatchMasterIncomeAmount.value != null)
        return Number(dispatchMasterIncomeAmount.value).toFixed(2);
      if (netPrice.value != null)
        return Number(netPrice.value).toFixed(2);
      if (!isFromDispatch.value)
        return detail.value.orderPrice.toFixed(2);
      const ratio = dispatchRatio.value != null ? dispatchRatio.value : (_a = detail.value.ratio) !== null && _a !== void 0 ? _a : 0;
      const finalAmount = detail.value.orderPrice * (1 - Number(ratio) / 100);
      return finalAmount.toFixed(2);
    });
    const markers = common_vendor.computed(() => {
      const list = [];
      if (detail.value.lat != 0 && detail.value.lng != 0) {
        list.push(new MapMarker({ id: 1, latitude: detail.value.lat, longitude: detail.value.lng, title: "客户位置" }));
      }
      if (masterLat.value != 0 && masterLng.value != 0) {
        list.push(new MapMarker({ id: 2, latitude: masterLat.value, longitude: masterLng.value, title: "我的位置" }));
      }
      return list;
    });
    const polyline = common_vendor.computed(() => {
      if (detail.value.lat == 0 || detail.value.lng == 0)
        return [];
      if (masterLat.value == 0 || masterLng.value == 0)
        return [];
      return [
        new MapPolyline({
          points: [
            new MapPolylinePoint({ latitude: masterLat.value, longitude: masterLng.value }),
            new MapPolylinePoint({ latitude: detail.value.lat, longitude: detail.value.lng })
          ],
          color: "#007AFF",
          width: 4,
          dottedLine: true
        })
      ];
    });
    common_vendor.onLoad((query) => {
      var _a, _b, _c, _d;
      id.value = (_a = query["id"]) !== null && _a !== void 0 ? _a : "";
      dispatchToken.value = (_b = query["dispatchToken"]) !== null && _b !== void 0 ? _b : "";
      const ratioParam = (_c = query["ratio"]) !== null && _c !== void 0 ? _c : "";
      if (ratioParam) {
        const parsedRatio = Number(ratioParam);
        dispatchRatio.value = isNaN(parsedRatio) ? null : parsedRatio;
      } else {
        dispatchRatio.value = null;
      }
      sharerId.value = (_d = query["sharer"]) !== null && _d !== void 0 ? _d : "";
      isFromDispatch.value = query["from"] == "dispatch" || dispatchToken.value !== "";
      loadMasterLocation();
      if (isFromDispatch.value && dispatchToken.value) {
        loadDispatchPreviewAndDetail();
      } else {
        loadDetail();
      }
    });
    const mapDetail = (item = null) => {
      var _a;
      const serverStatus = item.status || "";
      const mappedStatus = serverStatus == "COMPLETED" ? "done" : serverStatus == "ACCEPTED" || serverStatus == "DEPARTED" || serverStatus == "ARRIVED" ? "processing" : "new";
      const dispatchStatus = item.isDispatch === 1 ? item.dispatchId ? "accepted" : "dispatched" : "none";
      const masterId = item.dispatchId || item.orderReceivingId || "";
      return new common_mockData.MyOrder({
        id: item.id,
        orderNo: item.orderNo || "-",
        createTime: item.createTime ? new Date(item.createTime).getTime() : Date.now(),
        serviceType: item.serviceType || "-",
        plateNo: item.plateNo || "-",
        status: mappedStatus,
        masterId,
        orderReceivingId: item.orderReceivingId || "",
        dispatchStatus,
        dispatchToName: item.dispatchId || "",
        orderPrice: item.price ? Number(item.price) : 0,
        ratio: (_a = item.ratio) !== null && _a !== void 0 ? _a : null,
        remark: item.remark || "",
        customerPhone: item.customerPhone || "-",
        customerName: item.customerName || "",
        address: item.address || "",
        lat: item.lat || 0,
        lng: item.lng || 0,
        agencyId: item.agencyId || "",
        images: [],
        feedbackImages: [],
        serverStatus
      });
    };
    const QQ_MAP_KEY = common_api.MAP_CONFIG.tencentKey;
    const fetchReverseGeocode = (lat, lng, onSuccess) => {
      if (!QQ_MAP_KEY)
        return null;
      common_vendor.index.request({
        url: "https://apis.map.qq.com/ws/geocoder/v1/",
        method: "GET",
        data: new UTSJSONObject({
          location: `${lat},${lng}`,
          key: QQ_MAP_KEY,
          get_poi: 0
        }),
        success: (res) => {
          var _a, _b, _c;
          const anyRes = res;
          const data = anyRes === null || anyRes === void 0 ? null : anyRes.data;
          const addr = ((_b = (_a = data === null || data === void 0 ? null : data.result) === null || _a === void 0 ? null : _a.formatted_addresses) === null || _b === void 0 ? null : _b.recommend) || ((_c = data === null || data === void 0 ? null : data.result) === null || _c === void 0 ? null : _c.address) || "";
          if (addr)
            onSuccess(addr);
        }
      });
    };
    const resolveCustomerAddress = () => {
      if (detail.value.lat == 0 || detail.value.lng == 0)
        return null;
      fetchReverseGeocode(detail.value.lat, detail.value.lng, (addr) => {
        customerAddress.value = addr;
      });
    };
    const resolveMasterAddress = () => {
      if (masterLat.value == 0 || masterLng.value == 0)
        return null;
      fetchReverseGeocode(masterLat.value, masterLng.value, (addr) => {
        masterAddress.value = addr;
      });
    };
    const loadDetail = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        var _a;
        if (!id.value) {
          common_vendor.index.showToast({ title: "订单编号缺失", icon: "none" });
          return Promise.resolve(null);
        }
        try {
          const res = yield common_api.getOrderDetail(id.value);
          detail.value = mapDetail(res.order);
          netPrice.value = res.netPrice != null ? Number(res.netPrice) : null;
          if (dispatchRatio.value == null && ((_a = res.order) === null || _a === void 0 ? null : _a.ratio) != null) {
            const parsedRatio = Number(res.order.ratio);
            dispatchRatio.value = isNaN(parsedRatio) ? null : parsedRatio;
          }
          resolveCustomerAddress();
        } catch (err) {
          const found = UTS.arrayFind(common_mockData.globalOrders, (o) => {
            return o.id === id.value;
          });
          if (found != null) {
            detail.value = found;
            resolveCustomerAddress();
            return Promise.resolve(null);
          }
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "未找到订单信息", icon: "none" });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        }
      });
    };
    const checkAndShowDispatchModal = () => {
      const ratio = dispatchRatio.value != null ? dispatchRatio.value : 0;
      const finalAmount = detail.value.orderPrice * (1 - ratio / 100);
      const amountText = dispatchMasterIncomeAmount.value != null ? `¥${Number(dispatchMasterIncomeAmount.value).toFixed(2)}` : netPrice.value != null ? `¥${Number(netPrice.value).toFixed(2)}` : `¥${finalAmount.toFixed(2)}`;
      common_vendor.index.showModal(new UTSJSONObject({
        title: "收到派单邀请",
        content: `您收到一笔来自服务商的派单
师傅可得金额：${amountText}
是否接受该订单？`,
        confirmText: "立即接单",
        cancelText: "暂不处理",
        success: (res) => {
          if (res.confirm) {
            accept();
          }
        }
      }));
    };
    const loadDispatchPreviewAndDetail = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        if (!dispatchToken.value) {
          yield loadDetail();
          return Promise.resolve(null);
        }
        try {
          const preview = yield common_api.getDispatchPreview(dispatchToken.value);
          if (preview.orderId) {
            id.value = preview.orderId;
          }
          dispatchMasterIncomeAmount.value = preview.masterIncomeAmount != null ? Number(preview.masterIncomeAmount) / 100 : null;
          yield loadDetail();
          checkAndShowDispatchModal();
        } catch (err) {
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "派单链接已失效", icon: "none" });
          setTimeout(() => {
            common_vendor.index.switchTab({ url: "/pages/orders/orders" });
          }, 1200);
        }
      });
    };
    const formatTime = (ts) => {
      const d = new Date(ts);
      const pad = (n) => {
        return n < 10 ? "0" + n : "" + n;
      };
      return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    };
    const statusText = (s) => {
      if (s === "new")
        return "待接单";
      if (s === "processing") {
        const ss = detail.value.serverStatus;
        if (ss === "DEPARTED")
          return "已出发";
        if (ss === "ARRIVED")
          return "已到达";
        return "进行中";
      }
      return "已完成";
    };
    const statusClass = (s) => {
      if (s === "new")
        return "st-new";
      if (s === "processing")
        return "st-processing";
      return "st-done";
    };
    const getNextStatusLabel = (status) => {
      if (status === "ACCEPTED")
        return "确认出发";
      if (status === "DEPARTED")
        return "确认到达";
      if (status === "ARRIVED")
        return "完成订单";
      return "";
    };
    const getNextStatusValue = (status) => {
      if (status === "ACCEPTED")
        return "DEPARTED";
      if (status === "DEPARTED")
        return "ARRIVED";
      if (status === "ARRIVED")
        return "COMPLETED";
      return "";
    };
    const actionLabel = common_vendor.computed(() => {
      return getNextStatusLabel(detail.value.serverStatus);
    });
    const actionDisabled = common_vendor.computed(() => {
      return false;
    });
    const callPhone = (phone) => {
      if (!phone || phone == "-")
        return null;
      common_vendor.index.makePhoneCall({ phoneNumber: phone });
    };
    const openCustomerMap = () => {
      if (detail.value.lat == 0 || detail.value.lng == 0)
        return null;
      common_vendor.index.openLocation({
        latitude: detail.value.lat,
        longitude: detail.value.lng,
        name: customerAddress.value || detail.value.address,
        address: customerAddress.value || detail.value.address
      });
    };
    const accept = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        try {
          const storedUserId = common_vendor.index.getStorageSync("master_user_id");
          const operatorId = storedUserId || common_mockData.currentUserId.value;
          if (!operatorId) {
            common_vendor.index.showToast({ title: "请先登录", icon: "none" });
            return Promise.resolve(null);
          }
          let data = null;
          if (dispatchToken.value) {
            data = yield common_api.acceptDispatch(dispatchToken.value, operatorId);
          } else {
            data = yield common_api.acceptOrder({ orderId: detail.value.id, operatorId });
          }
          detail.value = mapDetail(data);
          common_vendor.index.showToast({ title: "接单成功", icon: "success" });
          common_vendor.index.switchTab({ url: "/pages/orders/orders" });
        } catch (err) {
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "接单失败", icon: "none" });
        }
      });
    };
    const applyTransfer = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        common_vendor.index.showModal(new UTSJSONObject({
          title: "申请转派",
          content: "确定要申请转派吗？申请后订单将从您的列表中移除。",
          editable: true,
          placeholderText: "请输入转派原因",
          success: (res) => {
            if (res.confirm) {
              common_vendor.index.showLoading({ title: "提交中..." });
              setTimeout(() => {
                common_vendor.index.hideLoading();
                const idx = common_mockData.globalOrders.findIndex((o) => {
                  return o.id === detail.value.id;
                });
                if (idx >= 0) {
                  common_mockData.globalOrders[idx].masterId = "";
                  common_mockData.globalOrders[idx].isTransferPending = true;
                  common_mockData.globalOrders[idx].transferReason = res.content;
                }
                common_vendor.index.showToast({ title: "已提交申请", icon: "success" });
                setTimeout(() => {
                  common_vendor.index.navigateBack();
                }, 1500);
              }, 1e3);
            }
          }
        }));
      });
    };
    const revokeDispatch = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        try {
          const storedUserId = common_vendor.index.getStorageSync("master_user_id");
          const operatorId = storedUserId || common_mockData.currentUserId.value;
          if (!operatorId) {
            common_vendor.index.showToast({ title: "请先登录", icon: "none" });
            return Promise.resolve(null);
          }
          const data = yield common_api.revokeDispatch(detail.value.id, operatorId);
          detail.value = mapDetail(data);
          common_vendor.index.showToast({ title: "已撤回", icon: "success" });
        } catch (err) {
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "撤回失败", icon: "none" });
        }
      });
    };
    const updateStatus = (nextStatus) => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        try {
          const storedUserId = common_vendor.index.getStorageSync("master_user_id");
          const operatorId = storedUserId || common_mockData.currentUserId.value;
          if (!operatorId) {
            common_vendor.index.showToast({ title: "请先登录", icon: "none" });
            return Promise.resolve(null);
          }
          let data = null;
          if (nextStatus === "COMPLETED") {
            data = yield common_api.completeOrder(detail.value.id, operatorId);
          } else {
            data = yield common_api.updateOrderStatus({ orderId: detail.value.id, status: nextStatus, operatorId });
          }
          detail.value = mapDetail(data);
          common_vendor.index.showToast({ title: "状态已更新", icon: "success" });
          if (nextStatus === "COMPLETED") {
            common_vendor.index.$emit("wallet:refresh");
          }
        } catch (err) {
          common_vendor.index.showToast({ title: (err === null || err === void 0 ? null : err.message) ? String(err.message) : "更新状态失败", icon: "none" });
        }
      });
    };
    const handleStatusAction = () => {
      return common_vendor.__awaiter(this, void 0, void 0, function* () {
        const current = detail.value.serverStatus;
        const nextStatus = getNextStatusValue(current);
        if (!nextStatus)
          return Promise.resolve(null);
        yield updateStatus(nextStatus);
      });
    };
    const onStatusActionClick = () => {
      if (!actionLabel.value || actionDisabled.value) {
        return null;
      }
      common_vendor.index.showModal(new UTSJSONObject({
        title: "确认操作",
        content: `确定要执行「${actionLabel.value}」吗？`,
        confirmText: "确定",
        cancelText: "取消",
        success: (res) => {
          if (res.confirm) {
            handleStatusAction();
          }
        }
      }));
    };
    const onApplyTransferClick = () => {
      common_vendor.index.showModal(new UTSJSONObject({
        title: "确认转派",
        content: "确定要申请转派吗？申请后订单将从您的列表中移除。",
        confirmText: "确认申请",
        cancelText: "取消",
        success: (res) => {
          if (res.confirm) {
            applyTransfer();
          }
        }
      }));
    };
    const onRevokeDispatchClick = () => {
      common_vendor.index.showModal(new UTSJSONObject({
        title: "确认撤回",
        content: "确定要撤回该订单的派发吗？",
        confirmText: "确认撤回",
        cancelText: "取消",
        success: (res) => {
          if (res.confirm) {
            revokeDispatch();
          }
        }
      }));
    };
    const loadMasterLocation = () => {
      common_vendor.index.getLocation(new UTSJSONObject({
        type: "gcj02",
        success: (res) => {
          masterLat.value = res.latitude;
          masterLng.value = res.longitude;
          resolveMasterAddress();
        },
        fail: () => {
          masterLat.value = 0;
          masterLng.value = 0;
          masterAddress.value = "";
        }
      }));
    };
    return (_ctx, _cache) => {
      "raw js";
      const __returned__ = common_vendor.e({
        a: common_vendor.t(common_vendor.unref(detail).orderNo),
        b: common_vendor.t(statusText(common_vendor.unref(detail).status)),
        c: common_vendor.n(statusClass(common_vendor.unref(detail).status)),
        d: common_vendor.t(formatTime(common_vendor.unref(detail).createTime)),
        e: common_vendor.t(common_vendor.unref(detail).serviceType),
        f: common_vendor.t(common_vendor.unref(detail).plateNo),
        g: common_vendor.t(common_vendor.unref(detail).customerPhone),
        h: common_vendor.o(($event) => {
          return callPhone(common_vendor.unref(detail).customerPhone);
        }),
        i: common_vendor.t(common_vendor.unref(customerAddress) || common_vendor.unref(detail).address || "-"),
        j: common_vendor.o(openCustomerMap),
        k: common_vendor.t(common_vendor.unref(detail).remark || "无"),
        l: !common_vendor.unref(detail).remark ? 1 : "",
        m: common_vendor.t(common_vendor.unref(displayAmount)),
        n: common_vendor.unref(detail).status === "new"
      }, common_vendor.unref(detail).status === "new" ? {} : {}, {
        o: common_vendor.unref(detail).status === "new"
      }, common_vendor.unref(detail).status === "new" ? {
        p: common_vendor.o(accept)
      } : {}, {
        q: common_vendor.unref(detail).status === "processing" || common_vendor.unref(detail).status === "done"
      }, common_vendor.unref(detail).status === "processing" || common_vendor.unref(detail).status === "done" ? common_vendor.e({
        r: common_vendor.unref(detail).status === "processing"
      }, common_vendor.unref(detail).status === "processing" ? {} : {}, {
        s: common_vendor.unref(detail).status === "processing"
      }, common_vendor.unref(detail).status === "processing" ? common_vendor.e({
        t: (common_vendor.unref(isMaster) || common_vendor.unref(isProvider)) && common_vendor.unref(actionLabel)
      }, (common_vendor.unref(isMaster) || common_vendor.unref(isProvider)) && common_vendor.unref(actionLabel) ? {
        v: common_vendor.t(common_vendor.unref(actionLabel)),
        w: common_vendor.unref(actionDisabled),
        x: common_vendor.o(onStatusActionClick)
      } : {}, {
        y: common_vendor.unref(isMaster)
      }, common_vendor.unref(isMaster) ? {
        z: common_vendor.o(onApplyTransferClick)
      } : {}, {
        A: common_vendor.unref(isProvider) && common_vendor.unref(detail).dispatchStatus === "dispatched"
      }, common_vendor.unref(isProvider) && common_vendor.unref(detail).dispatchStatus === "dispatched" ? {
        B: common_vendor.o(onRevokeDispatchClick)
      } : {}) : {}) : {}, {
        C: common_vendor.sei("orderMap", "map"),
        D: common_vendor.unref(mapCenterLat),
        E: common_vendor.unref(mapCenterLng),
        F: common_vendor.unref(markers),
        G: common_vendor.unref(polyline),
        H: common_vendor.t(common_vendor.unref(customerAddress) || common_vendor.unref(detail).address || "定位中..."),
        I: common_vendor.t(common_vendor.unref(detail).lat),
        J: common_vendor.t(common_vendor.unref(detail).lng),
        K: common_vendor.t(common_vendor.unref(masterAddress) || "定位中..."),
        L: common_vendor.t(common_vendor.unref(masterLat)),
        M: common_vendor.t(common_vendor.unref(masterLng)),
        N: common_vendor.o(openCustomerMap),
        O: common_vendor.sei(common_vendor.gei(_ctx, ""), "view")
      });
      return __returned__;
    };
  }
});
wx.createPage(_sfc_main);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/order-detail/order-detail.js.map
