package co.ke.xently.expense.tracker.server.routes

// language=JSON
val OPENAPI_SPEC_JSON: String = $$"""
{
  "openapi": "3.0.3",
  "info": {
    "title": "Project Expense Tracker API",
    "description": "Authenticated, project-scoped API for arbitrary partner projects, invitations, expenses, tranches, dashboards, and revenue splits. Legacy global finance endpoints in this specification are registered only when ENABLE_LEGACY_FINANCE_ROUTES=true.",
    "version": "1.0.0",
    "contact": {
      "name": "Sugarcane Farming Partnership",
      "email": "support@farm.co.ke"
    }
  },
  "servers": [
    {
      "url": "http://localhost:8081",
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
    { "name": "Events", "description": "Legacy Server-Sent Events stream; compatibility mode only" },
    { "name": "Projects", "description": "Authenticated project creation, membership, invitations, and scoped finance" },
    { "name": "Account", "description": "Authenticated user profile, project list, and invitation inbox" }
  ],
  "paths": {
    "/api/v1/me/profile": {
      "get": {
        "tags": ["Account"],
        "summary": "Get the authenticated user's profile",
        "security": [{ "BearerAuth": [] }],
        "responses": {
          "200": { "description": "User profile", "content": { "application/json": { "schema": { "$ref": "#/components/schemas/ProjectUser" } } } },
          "401": { "description": "Missing or invalid bearer token" },
          "404": { "description": "Profile is not registered" }
        }
      },
      "post": {
        "tags": ["Account"],
        "summary": "Register a unique username for the authenticated user",
        "security": [{ "BearerAuth": [] }],
        "requestBody": { "required": true, "content": { "application/json": { "schema": { "$ref": "#/components/schemas/RegistrationRequest" } } } },
        "responses": {
          "201": { "description": "Profile created" },
          "200": { "description": "Existing profile returned" },
          "409": { "description": "Username is already registered" }
        }
      }
    },
    "/api/v1/me/projects": {
      "get": {
        "tags": ["Account"],
        "summary": "List projects the authenticated user belongs to",
        "description": "defaultProjectId is set only when exactly one project is available.",
        "security": [{ "BearerAuth": [] }],
        "responses": { "200": { "description": "Caller projects", "content": { "application/json": { "schema": { "$ref": "#/components/schemas/MyProjectsResponse" } } } } }
      }
    },
    "/api/v1/me/invitations": {
      "get": {
        "tags": ["Account"],
        "summary": "Get invitations addressed to the authenticated user",
        "security": [{ "BearerAuth": [] }],
        "responses": { "200": { "description": "In-app project invitation messages", "content": { "application/json": { "schema": { "type": "array", "items": { "$ref": "#/components/schemas/ProjectInvitation" } } } } } }
      }
    },
    "/api/v1/projects": {
      "post": {
        "tags": ["Projects"],
        "summary": "Create a project with the caller as owner and first partner",
        "security": [{ "BearerAuth": [] }],
        "requestBody": { "required": true, "content": { "application/json": { "schema": { "$ref": "#/components/schemas/CreateProjectRequest" } } } },
        "responses": { "201": { "description": "Project created", "content": { "application/json": { "schema": { "$ref": "#/components/schemas/Project" } } } }, "401": { "description": "Unauthorized" } }
      }
    },
    "/api/v1/projects/{projectId}": {
      "get": {
        "tags": ["Projects"],
        "summary": "Get a project the caller is a partner in",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "responses": { "200": { "description": "Project details", "content": { "application/json": { "schema": { "$ref": "#/components/schemas/Project" } } } }, "404": { "description": "Project not found or caller is not a partner" } }
      }
    },
    "/api/v1/projects/{projectId}/members": {
      "get": {
        "tags": ["Projects"],
        "summary": "List project partners",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "responses": { "200": { "description": "Project partners", "content": { "application/json": { "schema": { "type": "array", "items": { "$ref": "#/components/schemas/ProjectMemberResponse" } } } } }, "404": { "description": "Project not found or caller is not a partner" } }
      }
    },
    "/api/v1/projects/{projectId}/invitations": {
      "post": {
        "tags": ["Projects"],
        "summary": "Invite a registered user by username",
        "description": "Only the project owner may invite; the invitation appears in the invitee's in-app inbox.",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "requestBody": { "required": true, "content": { "application/json": { "schema": { "$ref": "#/components/schemas/InviteProjectPartnerRequest" } } } },
        "responses": { "201": { "description": "Invitation created" }, "403": { "description": "Caller is not the owner" }, "409": { "description": "Already a partner or pending invitation exists" } }
      }
    },
    "/api/v1/invitations/{invitationId}/respond": {
      "post": {
        "tags": ["Projects"],
        "summary": "Accept or decline an invitation addressed to the caller",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "invitationId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "requestBody": { "required": true, "content": { "application/json": { "schema": { "$ref": "#/components/schemas/RespondToInvitationRequest" } } } },
        "responses": { "200": { "description": "Invitation response saved" }, "403": { "description": "Invitation belongs to another user" }, "409": { "description": "Invitation was already answered" } }
      }
    },
    "/api/v1/projects/{projectId}/expenses": {
      "get": {
        "tags": ["Projects", "Expenses"],
        "summary": "List expenses belonging to an accessible project",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "responses": { "200": { "description": "Project expenses", "content": { "application/json": { "schema": { "type": "array", "items": { "$ref": "#/components/schemas/Expense" } } } } }, "404": { "description": "Project not found or caller is not a partner" } }
      },
      "post": {
        "tags": ["Projects", "Expenses"],
        "summary": "Add a project expense split across current partners",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "requestBody": { "required": true, "content": { "application/json": { "schema": { "$ref": "#/components/schemas/CreateExpenseRequest" } } } },
        "responses": { "201": { "description": "Expense created with project-member splits" }, "404": { "description": "Project not found or caller is not a partner" } }
      }
    },
    "/api/v1/projects/{projectId}/expenses/{expenseId}": {
      "delete": {
        "tags": ["Projects", "Expenses"],
        "summary": "Delete an expense from an accessible project",
        "security": [{ "BearerAuth": [] }],
        "parameters": [
          { "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } },
          { "name": "expenseId", "in": "path", "required": true, "schema": { "type": "string" } }
        ],
        "responses": { "200": { "description": "Deleted" }, "404": { "description": "Project or expense not found" } }
      }
    },
    "/api/v1/projects/{projectId}/tranches": {
      "get": {
        "tags": ["Projects", "Tranches"],
        "summary": "List project tranche payments",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "responses": { "200": { "description": "Project tranches", "content": { "application/json": { "schema": { "type": "array", "items": { "$ref": "#/components/schemas/TranchePayment" } } } } }, "404": { "description": "Project not found or caller is not a partner" } }
      },
      "post": {
        "tags": ["Projects", "Tranches"],
        "summary": "Record a contribution for the authenticated partner",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "requestBody": { "required": true, "content": { "application/json": { "schema": { "$ref": "#/components/schemas/CreateTrancheRequest" } } } },
        "responses": { "201": { "description": "Contribution recorded" }, "403": { "description": "Partners may record only their own contributions" } }
      }
    },
    "/api/v1/projects/{projectId}/dashboard": {
      "get": {
        "tags": ["Projects", "Dashboard"],
        "summary": "Get the accessible project's dashboard",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "responses": { "200": { "description": "Project-scoped dashboard", "content": { "application/json": { "schema": { "$ref": "#/components/schemas/DashboardSummary" } } } }, "404": { "description": "Project not found or caller is not a partner" } }
      }
    },
    "/api/v1/projects/{projectId}/revenue-split": {
      "post": {
        "tags": ["Projects", "Revenue Split"],
        "summary": "Calculate a project-scoped revenue split",
        "security": [{ "BearerAuth": [] }],
        "parameters": [{ "name": "projectId", "in": "path", "required": true, "schema": { "type": "string" } }],
        "requestBody": { "required": true, "content": { "application/json": { "schema": { "$ref": "#/components/schemas/RevenueSplitRequest" } } } },
        "responses": { "200": { "description": "Project settlement calculation" }, "404": { "description": "Project not found or caller is not a partner" } }
      }
    },
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
    "securitySchemes": {
      "BearerAuth": { "type": "http", "scheme": "bearer", "bearerFormat": "JWT" }
    },
    "schemas": {
      "ProjectUser": {
        "type": "object",
        "required": ["id", "username", "displayName", "email"],
        "properties": {
          "id": { "type": "string" },
          "username": { "type": "string" },
          "displayName": { "type": "string" },
          "email": { "type": "string", "format": "email" }
        }
      },
      "Project": {
        "type": "object",
        "properties": {
          "id": { "type": "string" },
          "name": { "type": "string" },
          "description": { "type": "string" },
          "ownerUserId": { "type": "string" },
          "createdAtEpochMs": { "type": "integer", "format": "int64" }
        }
      },
      "MyProjectsResponse": {
        "type": "object",
        "properties": {
          "projects": { "type": "array", "items": { "$ref": "#/components/schemas/Project" } },
          "defaultProjectId": { "type": "string", "nullable": true }
        }
      },
      "ProjectMembership": {
        "type": "object",
        "properties": {
          "projectId": { "type": "string" },
          "userId": { "type": "string" },
          "role": { "type": "string", "enum": ["OWNER", "PARTNER"] },
          "joinedAtEpochMs": { "type": "integer", "format": "int64" }
        }
      },
      "ProjectMemberResponse": {
        "type": "object",
        "properties": {
          "user": { "$ref": "#/components/schemas/ProjectUser" },
          "membership": { "$ref": "#/components/schemas/ProjectMembership" }
        }
      },
      "ProjectInvitation": {
        "type": "object",
        "properties": {
          "id": { "type": "string" },
          "projectId": { "type": "string" },
          "projectName": { "type": "string" },
          "inviterUserId": { "type": "string" },
          "inviterUsername": { "type": "string" },
          "inviteeUserId": { "type": "string" },
          "inviteeUsername": { "type": "string" },
          "status": { "type": "string", "enum": ["PENDING", "ACCEPTED", "DECLINED"] },
          "createdAtEpochMs": { "type": "integer", "format": "int64" }
        }
      },
      "RegistrationRequest": {
        "type": "object",
        "required": ["username", "displayName"],
        "properties": {
          "username": { "type": "string", "minLength": 3, "maxLength": 32 },
          "displayName": { "type": "string" }
        }
      },
      "CreateProjectRequest": {
        "type": "object",
        "required": ["name"],
        "properties": {
          "name": { "type": "string" },
          "description": { "type": "string" }
        }
      },
      "InviteProjectPartnerRequest": {
        "type": "object",
        "required": ["username"],
        "properties": { "username": { "type": "string" } }
      },
      "RespondToInvitationRequest": {
        "type": "object",
        "required": ["accept"],
        "properties": { "accept": { "type": "boolean" } }
      },
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
