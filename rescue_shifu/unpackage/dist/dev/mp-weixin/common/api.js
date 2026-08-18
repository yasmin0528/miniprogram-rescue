"use strict";
const common_vendor = require("./vendor.js");
const API_CONFIG$1 = new UTSJSONObject({
  baseURL: "http://127.0.0.1:8080",
  timeout: 1e4
});
const MAP_CONFIG = new UTSJSONObject({
  tencentKey: "REDACTED_TENCENT_MAP_KEY"
});
const LOGIN_URL = `${API_CONFIG$1.baseURL}/api/wechat/login`;
const request = (options) => {
  return new Promise((resolve, reject) => {
    const fullUrl = options.url.startsWith("http") ? options.url : `${API_CONFIG$1.baseURL}${options.url}`;
    const token = common_vendor.index.getStorageSync("master_token");
    common_vendor.index.request({
      url: fullUrl,
      method: options.method || "GET",
      data: options.data || new UTSJSONObject({}),
      header: new UTSJSONObject(Object.assign(Object.assign({ "content-type": "application/json" }, token ? new UTSJSONObject({ Authorization: `Bearer ${token}` }) : new UTSJSONObject({})), options.header || new UTSJSONObject({}))),
      timeout: API_CONFIG$1.timeout,
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else {
          reject(new Error(`请求失败: ${res.statusCode}`));
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
};
const isApiSuccess = (res = null) => {
  var _a;
  if ((res === null || res === void 0 ? null : res.success) === true)
    return true;
  const code = String((_a = res === null || res === void 0 ? null : res.code) !== null && _a !== void 0 ? _a : "");
  return code === "200" || code === "00000";
};
const apiMessage = (res = null, fallback) => {
  const msg = (res === null || res === void 0 ? null : res.message) || (res === null || res === void 0 ? null : res.msg);
  return msg ? String(msg) : fallback;
};
const unwrapData = (res = null, fallback) => {
  if (isApiSuccess(res)) {
    return (res === null || res === void 0 ? null : res.data) || {};
  }
  const err = new Error(apiMessage(res, fallback));
  err.code = res === null || res === void 0 ? null : res.code;
  throw err;
};
let ShifuHallOrderRaw$1 = class ShifuHallOrderRaw extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          id: { type: String, optional: false },
          address: { type: String, optional: true },
          agencyId: { type: String, optional: true },
          agencyOrderType: { type: Number, optional: true },
          appointmentTime: { type: String, optional: true },
          customerId: { type: String, optional: true },
          customerName: { type: String, optional: true },
          customerPhone: { type: String, optional: true },
          isDeleted: { type: Number, optional: true },
          lat: { type: Number, optional: true },
          lng: { type: Number, optional: true },
          orderNo: { type: String, optional: true },
          orderType: { type: Number, optional: true },
          plateNo: { type: String, optional: true },
          price: { type: Number, optional: true },
          ratio: { type: Number, optional: true },
          remark: { type: String, optional: true },
          serviceType: { type: String, optional: true },
          status: { type: String, optional: true },
          createTime: { type: String, optional: true },
          netPrice: { type: Number, optional: true },
          masterIncomeAmount: { type: Number, optional: true },
          providerIncomeAmount: { type: Number, optional: true },
          isDispatch: { type: Number, optional: true },
          dispatchId: { type: String, optional: true },
          dispatchToken: { type: String, optional: true },
          settlementStatus: { type: String, optional: true },
          orderReceivingId: { type: String, optional: true }
        };
      },
      name: "ShifuHallOrderRaw"
    };
  }
  constructor(options, metadata = ShifuHallOrderRaw.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.id = this.__props__.id;
    this.address = this.__props__.address;
    this.agencyId = this.__props__.agencyId;
    this.agencyOrderType = this.__props__.agencyOrderType;
    this.appointmentTime = this.__props__.appointmentTime;
    this.customerId = this.__props__.customerId;
    this.customerName = this.__props__.customerName;
    this.customerPhone = this.__props__.customerPhone;
    this.isDeleted = this.__props__.isDeleted;
    this.lat = this.__props__.lat;
    this.lng = this.__props__.lng;
    this.orderNo = this.__props__.orderNo;
    this.orderType = this.__props__.orderType;
    this.plateNo = this.__props__.plateNo;
    this.price = this.__props__.price;
    this.ratio = this.__props__.ratio;
    this.remark = this.__props__.remark;
    this.serviceType = this.__props__.serviceType;
    this.status = this.__props__.status;
    this.createTime = this.__props__.createTime;
    this.netPrice = this.__props__.netPrice;
    this.masterIncomeAmount = this.__props__.masterIncomeAmount;
    this.providerIncomeAmount = this.__props__.providerIncomeAmount;
    this.isDispatch = this.__props__.isDispatch;
    this.dispatchId = this.__props__.dispatchId;
    this.dispatchToken = this.__props__.dispatchToken;
    this.settlementStatus = this.__props__.settlementStatus;
    this.orderReceivingId = this.__props__.orderReceivingId;
    delete this.__props__;
  }
};
let ShifuHallOrderItem$1 = class ShifuHallOrderItem extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          order: { type: ShifuHallOrderRaw$1, optional: false },
          netPrice: { type: Number, optional: true }
        };
      },
      name: "ShifuHallOrderItem"
    };
  }
  constructor(options, metadata = ShifuHallOrderItem.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.order = this.__props__.order;
    this.netPrice = this.__props__.netPrice;
    delete this.__props__;
  }
};
const getShifuHallOrders = (params) => {
  return request({
    url: "/api/shifu/orders/hall",
    method: "GET",
    data: params
  }).then((res = null) => {
    const data = unwrapData(res, "加载订单列表失败");
    if (!Array.isArray(data)) {
      throw new Error("加载订单列表失败");
    }
    return data.map((it = null) => {
      return new ShifuHallOrderItem$1({
        order: it === null || it === void 0 ? null : it.order,
        netPrice: (it === null || it === void 0 ? null : it.netPrice) != null ? Number(it.netPrice) : void 0
      });
    });
  });
};
let OrderDetailResp$1 = class OrderDetailResp extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          order: { type: "Unknown", optional: false },
          netPrice: { type: Number, optional: true }
        };
      },
      name: "OrderDetailResp"
    };
  }
  constructor(options, metadata = OrderDetailResp.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.order = this.__props__.order;
    this.netPrice = this.__props__.netPrice;
    delete this.__props__;
  }
};
const getOrderDetail = (orderId) => {
  return request({
    url: "/api/shifu/orders/detail",
    method: "GET",
    data: new UTSJSONObject({ orderId })
  }).then((res = null) => {
    const data = unwrapData(res, "加载订单详情失败");
    if (!(data === null || data === void 0 ? null : data.order)) {
      throw new Error("加载订单详情失败");
    }
    return new OrderDetailResp$1({
      order: data.order,
      netPrice: data.netPrice != null ? Number(data.netPrice) : void 0
    });
  });
};
let OrderListItem$1 = class OrderListItem extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          order: { type: ShifuHallOrderRaw$1, optional: false },
          netPrice: { type: Number, optional: true }
        };
      },
      name: "OrderListItem"
    };
  }
  constructor(options, metadata = OrderListItem.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.order = this.__props__.order;
    this.netPrice = this.__props__.netPrice;
    delete this.__props__;
  }
};
const getOrderList = (params) => {
  return request({
    url: "/api/order/list",
    method: "GET",
    data: params
  }).then((res = null) => {
    const data = unwrapData(res, "加载订单失败");
    if (!Array.isArray(data)) {
      throw new Error("加载订单失败");
    }
    return data.map((it = null) => {
      return new OrderListItem$1({
        order: it === null || it === void 0 ? null : it.order,
        netPrice: (it === null || it === void 0 ? null : it.netPrice) != null ? Number(it.netPrice) : void 0
      });
    });
  });
};
const acceptOrder = (params) => {
  return request({
    url: "/api/order/accept",
    method: "POST",
    data: params
  }).then((res = null) => {
    return unwrapData(res, "接单失败");
  });
};
let DispatchPreviewResp$1 = class DispatchPreviewResp extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          orderId: { type: String, optional: false },
          orderNo: { type: String, optional: true },
          serviceType: { type: String, optional: true },
          address: { type: String, optional: true },
          customerPhone: { type: String, optional: true },
          masterIncomeAmount: { type: Number, optional: true },
          isDispatch: { type: Number, optional: true }
        };
      },
      name: "DispatchPreviewResp"
    };
  }
  constructor(options, metadata = DispatchPreviewResp.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.orderId = this.__props__.orderId;
    this.orderNo = this.__props__.orderNo;
    this.serviceType = this.__props__.serviceType;
    this.address = this.__props__.address;
    this.customerPhone = this.__props__.customerPhone;
    this.masterIncomeAmount = this.__props__.masterIncomeAmount;
    this.isDispatch = this.__props__.isDispatch;
    delete this.__props__;
  }
};
const createDispatch = (orderId, operatorId, commissionAmount) => {
  return request({
    url: "/api/order/dispatch/create",
    method: "POST",
    data: new UTSJSONObject({ orderId, operatorId, commissionAmount })
  }).then((res = null) => {
    return unwrapData(res, "创建派单失败");
  });
};
const getDispatchPreview = (token) => {
  return request({
    url: "/api/order/dispatch/preview",
    method: "GET",
    data: new UTSJSONObject({ token })
  }).then((res = null) => {
    return unwrapData(res, "加载派单预览失败");
  });
};
const acceptDispatch = (token, operatorId) => {
  return request({
    url: "/api/order/dispatch/accept",
    method: "POST",
    data: new UTSJSONObject({ token, operatorId })
  }).then((res = null) => {
    return unwrapData(res, "接收派单失败");
  });
};
const revokeDispatch = (orderId, operatorId) => {
  return request({
    url: "/api/order/dispatch/revoke",
    method: "POST",
    data: new UTSJSONObject({ orderId, operatorId })
  }).then((res = null) => {
    return unwrapData(res, "撤回派单失败");
  });
};
const completeOrder = (orderId, operatorId) => {
  return request({
    url: "/api/order/complete",
    method: "POST",
    data: new UTSJSONObject({ orderId, operatorId })
  }).then((res = null) => {
    return unwrapData(res, "完成订单失败");
  });
};
const updateOrderStatus = (params) => {
  return request({
    url: "/api/order/status",
    method: "POST",
    data: params
  }).then((res = null) => {
    return unwrapData(res, "更新状态失败");
  });
};
let WithdrawCreateParams$1 = class WithdrawCreateParams extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          masterId: { type: String, optional: false },
          amountYuan: { type: Number, optional: false },
          accountId: { type: String, optional: false },
          verifyCode: { type: String, optional: true },
          requestId: { type: String, optional: false }
        };
      },
      name: "WithdrawCreateParams"
    };
  }
  constructor(options, metadata = WithdrawCreateParams.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.masterId = this.__props__.masterId;
    this.amountYuan = this.__props__.amountYuan;
    this.accountId = this.__props__.accountId;
    this.verifyCode = this.__props__.verifyCode;
    this.requestId = this.__props__.requestId;
    delete this.__props__;
  }
};
let WithdrawCreateResp$1 = class WithdrawCreateResp extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          applyNo: { type: String, optional: true },
          status: { type: String, optional: true },
          transferAmountYuan: { type: Number, optional: true },
          feeYuan: { type: Number, optional: true },
          netAmountYuan: { type: Number, optional: true }
        };
      },
      name: "WithdrawCreateResp"
    };
  }
  constructor(options, metadata = WithdrawCreateResp.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.applyNo = this.__props__.applyNo;
    this.status = this.__props__.status;
    this.transferAmountYuan = this.__props__.transferAmountYuan;
    this.feeYuan = this.__props__.feeYuan;
    this.netAmountYuan = this.__props__.netAmountYuan;
    delete this.__props__;
  }
};
let WithdrawApplyParams$1 = class WithdrawApplyParams extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          applyNo: { type: String, optional: false }
        };
      },
      name: "WithdrawApplyParams"
    };
  }
  constructor(options, metadata = WithdrawApplyParams.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.applyNo = this.__props__.applyNo;
    delete this.__props__;
  }
};
let WithdrawView$1 = class WithdrawView extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          applyNo: { type: String, optional: true },
          masterId: { type: String, optional: true },
          bizOrderNo: { type: String, optional: true },
          outBillNo: { type: String, optional: true },
          transferBillNo: { type: String, optional: true },
          status: { type: String, optional: true },
          transferAmountFen: { type: Number, optional: true },
          transferAmountYuan: { type: Number, optional: true },
          openId: { type: String, optional: true },
          applyTime: { type: String, optional: true },
          successTime: { type: String, optional: true },
          failReason: { type: String, optional: true },
          notifyTime: { type: String, optional: true }
        };
      },
      name: "WithdrawView"
    };
  }
  constructor(options, metadata = WithdrawView.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.applyNo = this.__props__.applyNo;
    this.masterId = this.__props__.masterId;
    this.bizOrderNo = this.__props__.bizOrderNo;
    this.outBillNo = this.__props__.outBillNo;
    this.transferBillNo = this.__props__.transferBillNo;
    this.status = this.__props__.status;
    this.transferAmountFen = this.__props__.transferAmountFen;
    this.transferAmountYuan = this.__props__.transferAmountYuan;
    this.openId = this.__props__.openId;
    this.applyTime = this.__props__.applyTime;
    this.successTime = this.__props__.successTime;
    this.failReason = this.__props__.failReason;
    this.notifyTime = this.__props__.notifyTime;
    delete this.__props__;
  }
};
let WithdrawReconcileResp$1 = class WithdrawReconcileResp extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          availableBalanceYuan: { type: Number, optional: true },
          freezeBalanceYuan: { type: Number, optional: true },
          list: { type: UTS.UTSType.withGenerics(Array, [WithdrawView$1]), optional: true }
        };
      },
      name: "WithdrawReconcileResp"
    };
  }
  constructor(options, metadata = WithdrawReconcileResp.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.availableBalanceYuan = this.__props__.availableBalanceYuan;
    this.freezeBalanceYuan = this.__props__.freezeBalanceYuan;
    this.list = this.__props__.list;
    delete this.__props__;
  }
};
const createWithdraw = (params) => {
  return request({
    url: "/api/pay/withdraw/create",
    method: "POST",
    data: params
  }).then((res = null) => {
    return unwrapData(res, "创建提现申请失败");
  });
};
const applyWithdraw = (params) => {
  return request({
    url: "/api/pay/withdraw/apply",
    method: "POST",
    data: params
  }).then((res = null) => {
    return unwrapData(res, "发起提现失败");
  });
};
const getWithdrawReconcile = (masterId) => {
  return request({
    url: "/api/pay/withdraw/reconcile",
    method: "GET",
    data: new UTSJSONObject({ masterId })
  }).then((res = null) => {
    return unwrapData(res, "查询钱包对账失败");
  });
};
const API_CONFIG = new UTSJSONObject({
  baseURL: "http://127.0.0.1:8080",
  timeout: 1e4
});
new UTSJSONObject({
  tencentKey: "REDACTED_TENCENT_MAP_KEY"
});
`${API_CONFIG.baseURL}/api/wechat/login`;
class ShifuHallOrderRaw2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          id: { type: String, optional: false },
          address: { type: String, optional: true },
          agencyId: { type: String, optional: true },
          agencyOrderType: { type: Number, optional: true },
          appointmentTime: { type: String, optional: true },
          customerId: { type: String, optional: true },
          customerName: { type: String, optional: true },
          customerPhone: { type: String, optional: true },
          isDeleted: { type: Number, optional: true },
          lat: { type: Number, optional: true },
          lng: { type: Number, optional: true },
          orderNo: { type: String, optional: true },
          orderType: { type: Number, optional: true },
          plateNo: { type: String, optional: true },
          price: { type: Number, optional: true },
          ratio: { type: Number, optional: true },
          remark: { type: String, optional: true },
          serviceType: { type: String, optional: true },
          status: { type: String, optional: true },
          createTime: { type: String, optional: true },
          netPrice: { type: Number, optional: true },
          masterIncomeAmount: { type: Number, optional: true },
          providerIncomeAmount: { type: Number, optional: true },
          isDispatch: { type: Number, optional: true },
          dispatchId: { type: String, optional: true },
          dispatchToken: { type: String, optional: true },
          settlementStatus: { type: String, optional: true },
          orderReceivingId: { type: String, optional: true }
        };
      },
      name: "ShifuHallOrderRaw"
    };
  }
  constructor(options, metadata = ShifuHallOrderRaw2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.id = this.__props__.id;
    this.address = this.__props__.address;
    this.agencyId = this.__props__.agencyId;
    this.agencyOrderType = this.__props__.agencyOrderType;
    this.appointmentTime = this.__props__.appointmentTime;
    this.customerId = this.__props__.customerId;
    this.customerName = this.__props__.customerName;
    this.customerPhone = this.__props__.customerPhone;
    this.isDeleted = this.__props__.isDeleted;
    this.lat = this.__props__.lat;
    this.lng = this.__props__.lng;
    this.orderNo = this.__props__.orderNo;
    this.orderType = this.__props__.orderType;
    this.plateNo = this.__props__.plateNo;
    this.price = this.__props__.price;
    this.ratio = this.__props__.ratio;
    this.remark = this.__props__.remark;
    this.serviceType = this.__props__.serviceType;
    this.status = this.__props__.status;
    this.createTime = this.__props__.createTime;
    this.netPrice = this.__props__.netPrice;
    this.masterIncomeAmount = this.__props__.masterIncomeAmount;
    this.providerIncomeAmount = this.__props__.providerIncomeAmount;
    this.isDispatch = this.__props__.isDispatch;
    this.dispatchId = this.__props__.dispatchId;
    this.dispatchToken = this.__props__.dispatchToken;
    this.settlementStatus = this.__props__.settlementStatus;
    this.orderReceivingId = this.__props__.orderReceivingId;
    delete this.__props__;
  }
}
class ShifuHallOrderItem2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          order: { type: ShifuHallOrderRaw2, optional: false },
          netPrice: { type: Number, optional: true }
        };
      },
      name: "ShifuHallOrderItem"
    };
  }
  constructor(options, metadata = ShifuHallOrderItem2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.order = this.__props__.order;
    this.netPrice = this.__props__.netPrice;
    delete this.__props__;
  }
}
class OrderDetailResp2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          order: { type: "Unknown", optional: false },
          netPrice: { type: Number, optional: true }
        };
      },
      name: "OrderDetailResp"
    };
  }
  constructor(options, metadata = OrderDetailResp2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.order = this.__props__.order;
    this.netPrice = this.__props__.netPrice;
    delete this.__props__;
  }
}
class OrderListItem2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          order: { type: ShifuHallOrderRaw2, optional: false },
          netPrice: { type: Number, optional: true }
        };
      },
      name: "OrderListItem"
    };
  }
  constructor(options, metadata = OrderListItem2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.order = this.__props__.order;
    this.netPrice = this.__props__.netPrice;
    delete this.__props__;
  }
}
class DispatchPreviewResp2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          orderId: { type: String, optional: false },
          orderNo: { type: String, optional: true },
          serviceType: { type: String, optional: true },
          address: { type: String, optional: true },
          customerPhone: { type: String, optional: true },
          masterIncomeAmount: { type: Number, optional: true },
          isDispatch: { type: Number, optional: true }
        };
      },
      name: "DispatchPreviewResp"
    };
  }
  constructor(options, metadata = DispatchPreviewResp2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.orderId = this.__props__.orderId;
    this.orderNo = this.__props__.orderNo;
    this.serviceType = this.__props__.serviceType;
    this.address = this.__props__.address;
    this.customerPhone = this.__props__.customerPhone;
    this.masterIncomeAmount = this.__props__.masterIncomeAmount;
    this.isDispatch = this.__props__.isDispatch;
    delete this.__props__;
  }
}
class WithdrawCreateParams2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          masterId: { type: String, optional: false },
          amountYuan: { type: Number, optional: false },
          accountId: { type: String, optional: false },
          verifyCode: { type: String, optional: true },
          requestId: { type: String, optional: false }
        };
      },
      name: "WithdrawCreateParams"
    };
  }
  constructor(options, metadata = WithdrawCreateParams2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.masterId = this.__props__.masterId;
    this.amountYuan = this.__props__.amountYuan;
    this.accountId = this.__props__.accountId;
    this.verifyCode = this.__props__.verifyCode;
    this.requestId = this.__props__.requestId;
    delete this.__props__;
  }
}
class WithdrawCreateResp2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          applyNo: { type: String, optional: true },
          status: { type: String, optional: true },
          transferAmountYuan: { type: Number, optional: true },
          feeYuan: { type: Number, optional: true },
          netAmountYuan: { type: Number, optional: true }
        };
      },
      name: "WithdrawCreateResp"
    };
  }
  constructor(options, metadata = WithdrawCreateResp2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.applyNo = this.__props__.applyNo;
    this.status = this.__props__.status;
    this.transferAmountYuan = this.__props__.transferAmountYuan;
    this.feeYuan = this.__props__.feeYuan;
    this.netAmountYuan = this.__props__.netAmountYuan;
    delete this.__props__;
  }
}
class WithdrawApplyParams2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          applyNo: { type: String, optional: false }
        };
      },
      name: "WithdrawApplyParams"
    };
  }
  constructor(options, metadata = WithdrawApplyParams2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.applyNo = this.__props__.applyNo;
    delete this.__props__;
  }
}
class WithdrawView2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          applyNo: { type: String, optional: true },
          masterId: { type: String, optional: true },
          bizOrderNo: { type: String, optional: true },
          outBillNo: { type: String, optional: true },
          transferBillNo: { type: String, optional: true },
          status: { type: String, optional: true },
          transferAmountFen: { type: Number, optional: true },
          transferAmountYuan: { type: Number, optional: true },
          openId: { type: String, optional: true },
          applyTime: { type: String, optional: true },
          successTime: { type: String, optional: true },
          failReason: { type: String, optional: true },
          notifyTime: { type: String, optional: true }
        };
      },
      name: "WithdrawView"
    };
  }
  constructor(options, metadata = WithdrawView2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.applyNo = this.__props__.applyNo;
    this.masterId = this.__props__.masterId;
    this.bizOrderNo = this.__props__.bizOrderNo;
    this.outBillNo = this.__props__.outBillNo;
    this.transferBillNo = this.__props__.transferBillNo;
    this.status = this.__props__.status;
    this.transferAmountFen = this.__props__.transferAmountFen;
    this.transferAmountYuan = this.__props__.transferAmountYuan;
    this.openId = this.__props__.openId;
    this.applyTime = this.__props__.applyTime;
    this.successTime = this.__props__.successTime;
    this.failReason = this.__props__.failReason;
    this.notifyTime = this.__props__.notifyTime;
    delete this.__props__;
  }
}
class WithdrawReconcileResp2 extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          availableBalanceYuan: { type: Number, optional: true },
          freezeBalanceYuan: { type: Number, optional: true },
          list: { type: UTS.UTSType.withGenerics(Array, [WithdrawView2]), optional: true }
        };
      },
      name: "WithdrawReconcileResp"
    };
  }
  constructor(options, metadata = WithdrawReconcileResp2.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.availableBalanceYuan = this.__props__.availableBalanceYuan;
    this.freezeBalanceYuan = this.__props__.freezeBalanceYuan;
    this.list = this.__props__.list;
    delete this.__props__;
  }
}
exports.LOGIN_URL = LOGIN_URL;
exports.MAP_CONFIG = MAP_CONFIG;
exports.WithdrawApplyParams = WithdrawApplyParams2;
exports.WithdrawCreateParams = WithdrawCreateParams2;
exports.acceptDispatch = acceptDispatch;
exports.acceptOrder = acceptOrder;
exports.applyWithdraw = applyWithdraw;
exports.completeOrder = completeOrder;
exports.createDispatch = createDispatch;
exports.createWithdraw = createWithdraw;
exports.getDispatchPreview = getDispatchPreview;
exports.getOrderDetail = getOrderDetail;
exports.getOrderList = getOrderList;
exports.getShifuHallOrders = getShifuHallOrders;
exports.getWithdrawReconcile = getWithdrawReconcile;
exports.revokeDispatch = revokeDispatch;
exports.updateOrderStatus = updateOrderStatus;
//# sourceMappingURL=../../.sourcemap/mp-weixin/common/api.js.map
