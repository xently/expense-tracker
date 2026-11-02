package co.ke.xently.expense.tracker.server.routes

// language=JSON
val OPENAPI_SPEC_JSON: String = $$"""
{
  "openapi": "3.0.3",
  "info": {
    "title": "Sugarcane Season Expense Tracker API",
    "description": "Comprehensive REST and SSE API for tracking sugarcane farming and planting season expenses, managing partner tranche contributions (Harrison, Herman, Vin), calculating pro-rata revenue splits, and automating overdue notifications.",
    "version": "1.0.0",
    "contact": {
      "name": "Sugarcane Farming Partnership",
      "email": "support@farm.co.ke"
    }
  },
  "servers": [
    {
      "url": "http://localhost:8080",
      "description": "Local Development Server"
    }
  ],
  "tags": [
    { "name": "Health", "description": "System health and status checks" },
    { "name": "Dashboard", "description": "Aggregated ledger and financial metrics" },
    { "name": "Expenses", "description": "Expense logging, category tracking, and equal 1/3 splits" },
    { "name": "Tranches", "description": "Partner installment contributions and balance management" },
    { "name": "Partners", "description": "Partner ledger summaries and compliance status" },
    { "name": "Revenue Split", "description": "End-of-season factory payout settlement calculator" },
    { "name": "Notifications", "description": "Overdue reminders and FCM push alerts" },
    { "name": "Events", "description": "Server-Sent Events (SSE) real-time stream" }
  ],
  "paths": {
    "/api/v1/health": {
      "get": {
        "tags": ["Health"],
        "summary": "Health check",
        "description": "Returns current server health status.",
        "responses": {
          "200": {
            "description": "Server is healthy",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "status": { "type": "string", "example": "healthy" },
                    "service": { "type": "string", "example": "sugarcane-expense-tracker" }
                  }
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/dashboard": {
      "get": {
        "tags": ["Dashboard"],
        "summary": "Get dashboard summary",
        "description": "Aggregates total season expenses, cash tranche contributions, net balances, category breakdowns, and compliance statuses.",
        "parameters": [
          {
            "name": "asOfDate",
            "in": "query",
            "required": false,
            "description": "ISO-8601 evaluation date for due date calculations (e.g. 2026-11-02)",
            "schema": { "type": "string", "format": "date", "example": "2026-11-02" }
          }
        ],
        "responses": {
          "200": {
            "description": "Dashboard summary metrics",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/DashboardSummary" }
              }
            }
          }
        }
      }
    },
    "/api/v1/expenses": {
      "get": {
        "tags": ["Expenses"],
        "summary": "List all season expenses",
        "description": "Retrieves all logged farming season expenses with their split obligations.",
        "responses": {
          "200": {
            "description": "List of expenses",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/Expense" }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": ["Expenses"],
        "summary": "Record a new farming expense",
        "description": "Logs an expense and automatically splits the total amount equally (1/3 each) with due date set to the 10th of the following month.",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CreateExpenseRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Expense successfully recorded",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Expense" }
              }
            }
          }
        }
      }
    },
    "/api/v1/expenses/{id}": {
      "get": {
        "tags": ["Expenses"],
        "summary": "Get expense details",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string" } }
        ],
        "responses": {
          "200": {
            "description": "Expense found",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/Expense" }
              }
            }
          },
          "404": { "description": "Expense not found" }
        }
      },
      "delete": {
        "tags": ["Expenses"],
        "summary": "Delete an expense",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string" } }
        ],
        "responses": {
          "200": { "description": "Expense successfully deleted" },
          "404": { "description": "Expense not found" }
        }
      }
    },
    "/api/v1/tranches": {
      "get": {
        "tags": ["Tranches"],
        "summary": "List partner tranche payments",
        "parameters": [
          {
            "name": "partnerId",
            "in": "query",
            "required": false,
            "description": "Filter payments by partner identifier (e.g. HARRISON, HERMAN, VIN)",
            "schema": { "type": "string" }
          }
        ],
        "responses": {
          "200": {
            "description": "List of tranche payments",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/TranchePayment" }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": ["Tranches"],
        "summary": "Record a partner tranche installment",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/CreateTrancheRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Tranche payment recorded",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/TranchePayment" }
              }
            }
          }
        }
      }
    },
    "/api/v1/partners": {
      "get": {
        "tags": ["Partners"],
        "summary": "List all farming partners",
        "responses": {
          "200": {
            "description": "List of partners",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/Partner" }
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/partners/{id}/ledger": {
      "get": {
        "tags": ["Partners"],
        "summary": "Get individual partner ledger balance & status",
        "parameters": [
          { "name": "id", "in": "path", "required": true, "schema": { "type": "string" } },
          { "name": "asOfDate", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } }
        ],
        "responses": {
          "200": {
            "description": "Partner ledger summary",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/PartnerLedgerSummary" }
              }
            }
          },
          "404": { "description": "Partner not found" }
        }
      }
    },
    "/api/v1/revenue-split": {
      "post": {
        "tags": ["Revenue Split"],
        "summary": "Calculate end-of-season revenue split",
        "description": "Computes final partner investment percentages, distributes net factory payout pro-rata, recovers direct out-of-pocket expenses, and calculates net profits & ROI.",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/RevenueSplitRequest" }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Settlement calculation",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/SeasonRevenueCalculation" }
              }
            }
          }
        }
      }
    },
    "/api/v1/notifications": {
      "get": {
        "tags": ["Notifications"],
        "summary": "List dispatched notifications",
        "parameters": [
          { "name": "partnerId", "in": "query", "required": false, "schema": { "type": "string" } }
        ],
        "responses": {
          "200": {
            "description": "List of notifications",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/AppNotification" }
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/notifications/trigger-check": {
      "post": {
        "tags": ["Notifications"],
        "summary": "Manually trigger overdue notification check",
        "description": "Evaluates overdue balances past the 10th of the following month and dispatches FCM push notifications.",
        "parameters": [
          { "name": "asOfDate", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } }
        ],
        "responses": {
          "200": {
            "description": "Check results",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "dispatchedCount": { "type": "integer" },
                    "notifications": {
                      "type": "array",
                      "items": { "$ref": "#/components/schemas/AppNotification" }
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/events/stream": {
      "get": {
        "tags": ["Events"],
        "summary": "Server-Sent Events (SSE) real-time stream",
        "description": "Streams real-time updates for expenses, tranches, notifications, and ledger changes.",
        "responses": {
          "200": {
            "description": "SSE text stream (text/event-stream)",
            "content": {
              "text/event-stream": {}
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "CreateExpenseRequest": {
        "type": "object",
        "required": ["title", "category", "totalAmount", "transactionDate", "payingPartnerId"],
        "properties": {
          "title": { "type": "string", "example": "DAP Fertilizer 50kg bags (x10)" },
          "category": { "type": "string", "example": "FERTILIZER", "enum": ["LAND_PREP", "SEEDLINGS", "WEEDING", "FERTILIZER", "CHOPPING", "TRANSPORT", "HARVEST_OPERATIONS", "MISCELLANEOUS"] },
          "totalAmount": { "type": "number", "format": "double", "example": 60000.0 },
          "currency": { "type": "string", "example": "KES" },
          "transactionDate": { "type": "string", "format": "date", "example": "2026-05-15" },
          "payingPartnerId": { "type": "string", "example": "HARRISON" },
          "notes": { "type": "string", "example": "Purchased from AgriVet Supplies" }
        }
      },
      "ExpenseSplit": {
        "type": "object",
        "properties": {
          "partnerId": { "type": "string", "example": "HERMAN" },
          "shareAmount": { "type": "number", "format": "double", "example": 20000.0 },
          "dueDate": { "type": "string", "format": "date", "example": "2026-06-10" }
        }
      },
      "Expense": {
        "type": "object",
        "properties": {
          "id": { "type": "string", "example": "exp-101" },
          "title": { "type": "string", "example": "DAP Fertilizer 50kg bags" },
          "category": { "type": "string", "example": "FERTILIZER" },
          "totalAmount": { "type": "number", "format": "double", "example": 60000.0 },
          "currency": { "type": "string", "example": "KES" },
          "transactionDate": { "type": "string", "format": "date", "example": "2026-05-15" },
          "payingPartnerId": { "type": "string", "example": "HARRISON" },
          "notes": { "type": "string", "example": "Purchased from AgriVet" },
          "splits": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/ExpenseSplit" }
          },
          "createdAtEpochMs": { "type": "integer", "format": "int64" }
        }
      },
      "CreateTrancheRequest": {
        "type": "object",
        "required": ["partnerId", "amount", "paymentDate"],
        "properties": {
          "partnerId": { "type": "string", "example": "HERMAN" },
          "amount": { "type": "number", "format": "double", "example": 20000.0 },
          "paymentDate": { "type": "string", "format": "date", "example": "2026-06-05" },
          "notes": { "type": "string", "example": "M-Pesa payment for June obligation" },
          "referenceNumber": { "type": "string", "example": "QKH8912KLM" }
        }
      },
      "TranchePayment": {
        "type": "object",
        "properties": {
          "id": { "type": "string", "example": "tr-201" },
          "partnerId": { "type": "string", "example": "HERMAN" },
          "amount": { "type": "number", "format": "double", "example": 20000.0 },
          "paymentDate": { "type": "string", "format": "date", "example": "2026-06-05" },
          "notes": { "type": "string", "example": "M-Pesa payment" },
          "referenceNumber": { "type": "string", "example": "QKH8912KLM" },
          "createdAtEpochMs": { "type": "integer", "format": "int64" }
        }
      },
      "Partner": {
        "type": "object",
        "properties": {
          "id": { "type": "string", "example": "HARRISON" },
          "name": { "type": "string", "example": "Harrison" },
          "email": { "type": "string", "example": "harrison@farm.co.ke" },
          "phone": { "type": "string", "example": "+254712345678" },
          "fcmToken": { "type": "string", "nullable": true }
        }
      },
      "PartnerLedgerSummary": {
        "type": "object",
        "properties": {
          "partnerId": { "type": "string", "example": "HERMAN" },
          "partnerName": { "type": "string", "example": "Herman" },
          "directOutOfPocket": { "type": "number", "format": "double", "example": 0.0 },
          "cashTranches": { "type": "number", "format": "double", "example": 20000.0 },
          "totalContributed": { "type": "number", "format": "double", "example": 20000.0 },
          "totalObligated": { "type": "number", "format": "double", "example": 20000.0 },
          "netBalance": { "type": "number", "format": "double", "example": 0.0 },
          "overdueAmount": { "type": "number", "format": "double", "example": 0.0 },
          "complianceStatus": { "type": "string", "enum": ["PAID_IN_FULL", "PARTIAL", "OVERDUE"], "example": "PAID_IN_FULL" },
          "investmentSharePercentage": { "type": "number", "format": "double", "example": 33.33 }
        }
      },
      "DashboardSummary": {
        "type": "object",
        "properties": {
          "totalExpenses": { "type": "number", "format": "double", "example": 60000.0 },
          "totalContributions": { "type": "number", "format": "double", "example": 60000.0 },
          "totalOutstandingObligations": { "type": "number", "format": "double", "example": 0.0 },
          "totalOverdueAmount": { "type": "number", "format": "double", "example": 0.0 },
          "partnerSummaries": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/PartnerLedgerSummary" }
          },
          "categoryBreakdowns": {
            "type": "object",
            "additionalProperties": { "type": "number", "format": "double" }
          },
          "recentExpenses": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/Expense" }
          },
          "recentTranches": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/TranchePayment" }
          },
          "notifications": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/AppNotification" }
          }
        }
      },
      "RevenueSplitRequest": {
        "type": "object",
        "required": ["grossFactoryPayout", "harvestOperationsExpenses"],
        "properties": {
          "grossFactoryPayout": { "type": "number", "format": "double", "example": 500000.0 },
          "harvestOperationsExpenses": { "type": "number", "format": "double", "example": 80000.0 }
        }
      },
      "PartnerSettlement": {
        "type": "object",
        "properties": {
          "partnerId": { "type": "string", "example": "HARRISON" },
          "partnerName": { "type": "string", "example": "Harrison" },
          "actualInvestment": { "type": "number", "format": "double", "example": 80000.0 },
          "investmentSharePercentage": { "type": "number", "format": "double", "example": 40.0 },
          "grossPayoutShare": { "type": "number", "format": "double", "example": 200000.0 },
          "netPayoutShare": { "type": "number", "format": "double", "example": 168000.0 },
          "directOutOfPocketRecovery": { "type": "number", "format": "double", "example": 80000.0 },
          "finalTotalPayout": { "type": "number", "format": "double", "example": 168000.0 },
          "netProfit": { "type": "number", "format": "double", "example": 88000.0 },
          "returnOnInvestmentPercentage": { "type": "number", "format": "double", "example": 110.0 }
        }
      },
      "SeasonRevenueCalculation": {
        "type": "object",
        "properties": {
          "grossFactoryPayout": { "type": "number", "format": "double", "example": 500000.0 },
          "harvestOperationsExpenses": { "type": "number", "format": "double", "example": 80000.0 },
          "netFactoryPayout": { "type": "number", "format": "double", "example": 420000.0 },
          "totalProjectInvestment": { "type": "number", "format": "double", "example": 200000.0 },
          "partnerSettlements": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/PartnerSettlement" }
          }
        }
      },
      "AppNotification": {
        "type": "object",
        "properties": {
          "id": { "type": "string", "example": "notif-301" },
          "partnerId": { "type": "string", "example": "VIN" },
          "partnerName": { "type": "string", "example": "Vin" },
          "title": { "type": "string", "example": "Overdue Expense Contribution" },
          "message": { "type": "string", "example": "Reminder: Your farming expense share of KES 20,000 for May is past due (due June 10)." },
          "amountOverdue": { "type": "number", "format": "double", "example": 20000.0 },
          "monthYear": { "type": "string", "example": "2026-05" },
          "timestampEpochMs": { "type": "integer", "format": "int64" },
          "isSent": { "type": "boolean", "example": true },
          "fcmMessageId": { "type": "string", "nullable": true }
        }
      }
    }
  }
}
""".trimIndent()
