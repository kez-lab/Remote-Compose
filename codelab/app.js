const STEP_COUNT = 10;
const COMPLETED_KEY = "remote-compose-beginner-codelab.completed.v4";
const CHECK_KEY = "remote-compose-beginner-codelab.checks.v4";

const stepNames = [
  "완성 화면 보기",
  "SDUI 이해하기",
  "샘플 실행하기",
  "서버에서 첫 UI",
  "문서 만들기",
  "문서 안의 상태",
  "목록과 상세",
  "앱에 작업 요청하기",
  "Ktor로 문서 보내기",
  "정리와 다음 단계",
];

const stepArticles = [...document.querySelectorAll("[data-step]")];
const stepButtons = [...document.querySelectorAll("[data-step-target]")];
const previousButton = document.querySelector("#previous-step");
const nextButton = document.querySelector("#next-step");
const completeButton = document.querySelector("#complete-step");
const previousLabel = document.querySelector("#previous-label");
const nextLabel = document.querySelector("#next-label");
const completionLabel = document.querySelector("#completion-label");
const progressBar = document.querySelector("#top-progress-bar");
const menuButton = document.querySelector("#menu-button");
const drawerOverlay = document.querySelector("#drawer-overlay");
const resetButton = document.querySelector("#reset-progress");
const lesson = document.querySelector("#lesson");
const runtimeToggleButton = document.querySelector("#runtime-toggle");
const runtimeResetButton = document.querySelector("#runtime-reset");
const runtimeStateValue = document.querySelector("#runtime-state-value");
const runtimeChildFalse = document.querySelector("#runtime-child-false");
const runtimeChildTrue = document.querySelector("#runtime-child-true");
const runtimeEvent = document.querySelector("#runtime-event");

let currentStep = readStepFromHash();
let completed = readSet(COMPLETED_KEY);
let checks = readObject(CHECK_KEY);
let runtimeDone = false;

function renderRuntimeLab(message) {
  if (!runtimeStateValue) return;
  runtimeStateValue.textContent = runtimeDone ? "1" : "0";
  runtimeChildFalse?.classList.toggle("is-active", !runtimeDone);
  runtimeChildTrue?.classList.toggle("is-active", runtimeDone);
  if (runtimeEvent) runtimeEvent.textContent = message;
}

function readStepFromHash() {
  const match = window.location.hash.match(/^#step-(\d+)$/);
  const parsed = match ? Number(match[1]) : 0;
  return Number.isInteger(parsed) && parsed >= 0 && parsed < STEP_COUNT ? parsed : 0;
}

function readSet(key) {
  try {
    const value = JSON.parse(localStorage.getItem(key) || "[]");
    return new Set(Array.isArray(value) ? value.filter(Number.isInteger) : []);
  } catch {
    return new Set();
  }
}

function readObject(key) {
  try {
    const value = JSON.parse(localStorage.getItem(key) || "{}");
    return value && typeof value === "object" ? value : {};
  } catch {
    return {};
  }
}

function renderStep(nextStep, updateHash = true, moveFocus = true) {
  currentStep = Math.min(Math.max(nextStep, 0), STEP_COUNT - 1);

  stepArticles.forEach((article) => {
    article.hidden = Number(article.dataset.step) !== currentStep;
  });

  stepButtons.forEach((button) => {
    const index = Number(button.dataset.stepTarget);
    if (index === currentStep) button.setAttribute("aria-current", "step");
    else button.removeAttribute("aria-current");
    button.classList.toggle("is-complete", completed.has(index));
    const number = button.querySelector("span");
    if (number) number.textContent = completed.has(index) ? "✓" : String(index + 1);
  });

  previousButton.disabled = currentStep === 0;
  nextButton.disabled = currentStep === STEP_COUNT - 1;
  previousLabel.textContent = currentStep === 0 ? "첫 단계" : stepNames[currentStep - 1];
  nextLabel.textContent = currentStep === STEP_COUNT - 1 ? "마지막 단계" : stepNames[currentStep + 1];

  const isComplete = completed.has(currentStep);
  completeButton.classList.toggle("is-complete", isComplete);
  completeButton.textContent = isComplete ? "완료됨 · 취소" : "이 단계 완료";

  completionLabel.textContent = `${completed.size} / ${STEP_COUNT} 완료`;
  progressBar.style.width = `${((currentStep + 1) / STEP_COUNT) * 100}%`;

  if (updateHash) history.replaceState(null, "", `#step-${currentStep}`);
  document.title = `${currentStep + 1}. ${stepNames[currentStep]} · Remote Compose 처음 시작하기`;
  closeDrawer();

  window.scrollTo({ top: 0, behavior: "instant" });
  if (moveFocus) lesson.focus({ preventScroll: true });
}

function toggleCurrentComplete() {
  if (completed.has(currentStep)) completed.delete(currentStep);
  else completed.add(currentStep);
  localStorage.setItem(COMPLETED_KEY, JSON.stringify([...completed]));
  renderStep(currentStep, false, false);
}

function openDrawer() {
  document.body.classList.add("drawer-open");
  menuButton.setAttribute("aria-expanded", "true");
  drawerOverlay.hidden = false;
}

function closeDrawer() {
  document.body.classList.remove("drawer-open");
  menuButton.setAttribute("aria-expanded", "false");
  drawerOverlay.hidden = true;
}

function toggleDrawer() {
  if (document.body.classList.contains("drawer-open")) closeDrawer();
  else openDrawer();
}

async function copyText(text) {
  if (navigator.clipboard?.writeText && window.isSecureContext) {
    try {
      await Promise.race([
        navigator.clipboard.writeText(text),
        new Promise((_, reject) =>
          window.setTimeout(() => reject(new Error("clipboard timeout")), 500),
        ),
      ]);
      return true;
    } catch {
      // Continue with the selection fallback.
    }
  }

  const textarea = document.createElement("textarea");
  textarea.value = text;
  textarea.setAttribute("readonly", "");
  textarea.style.position = "fixed";
  textarea.style.inset = "0 auto auto -9999px";
  document.body.append(textarea);
  textarea.select();
  textarea.setSelectionRange(0, textarea.value.length);
  const copied = document.execCommand("copy");
  textarea.remove();
  return copied;
}

function selectCode(block) {
  const code = block.querySelector("code");
  if (!code) return;
  const selection = window.getSelection();
  const range = document.createRange();
  range.selectNodeContents(code);
  selection.removeAllRanges();
  selection.addRange(range);
}

function setupCopyButtons() {
  document.querySelectorAll(".code-block").forEach((block) => {
    const button = block.querySelector(".copy-button");
    if (!button) return;
    button.setAttribute("aria-label", `${block.dataset.language || "코드"} 코드 복사`);
    button.addEventListener("click", async () => {
      const code = block.querySelector("code")?.textContent || "";
      const copied = await copyText(code);
      button.classList.add("is-copied");
      if (copied) {
        button.textContent = "복사됨";
      } else {
        selectCode(block);
        button.textContent = "선택됨 · ⌘C";
      }
      window.setTimeout(() => {
        button.textContent = "코드 복사";
        button.classList.remove("is-copied");
      }, 1800);
    });
  });
}

function setupChecklist() {
  document.querySelectorAll("[data-check]").forEach((input) => {
    input.checked = Boolean(checks[input.dataset.check]);
    input.addEventListener("change", () => {
      checks[input.dataset.check] = input.checked;
      localStorage.setItem(CHECK_KEY, JSON.stringify(checks));
    });
  });
}

stepButtons.forEach((button) => {
  button.addEventListener("click", () => renderStep(Number(button.dataset.stepTarget)));
});

previousButton.addEventListener("click", () => {
  if (currentStep > 0) renderStep(currentStep - 1);
});

nextButton.addEventListener("click", () => {
  if (currentStep < STEP_COUNT - 1) renderStep(currentStep + 1);
});

completeButton.addEventListener("click", toggleCurrentComplete);
menuButton.addEventListener("click", toggleDrawer);
drawerOverlay.addEventListener("click", closeDrawer);

resetButton?.addEventListener("click", () => {
  completed = new Set();
  checks = {};
  localStorage.removeItem(COMPLETED_KEY);
  localStorage.removeItem(CHECK_KEY);
  document.querySelectorAll("[data-check]").forEach((input) => { input.checked = false; });
  renderStep(currentStep, false, false);
});

runtimeToggleButton?.addEventListener("click", () => {
  const previous = runtimeDone ? 1 : 0;
  runtimeDone = !runtimeDone;
  const next = runtimeDone ? 1 : 0;
  renderRuntimeLab(`click action이 #42를 ${previous}에서 ${next}(으)로 변경했습니다. child ${next}만 표시합니다.`);
});

runtimeResetButton?.addEventListener("click", () => {
  runtimeDone = false;
  renderRuntimeLab("#42를 0으로 초기화했습니다. child 0만 표시합니다.");
});

window.addEventListener("hashchange", () => renderStep(readStepFromHash(), false));
window.addEventListener("resize", () => {
  if (window.innerWidth > 900) closeDrawer();
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") closeDrawer();
  if (!event.altKey) return;
  if (event.key === "ArrowLeft" && currentStep > 0) renderStep(currentStep - 1);
  if (event.key === "ArrowRight" && currentStep < STEP_COUNT - 1) renderStep(currentStep + 1);
});

setupCopyButtons();
setupChecklist();
renderStep(currentStep, false, false);
