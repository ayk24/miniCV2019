int main();

func int main() {
  int a, b, c;
  a = 3;
  b = 1;
  // 演算子の優先順位を確認
  if (true || false && a == 2) {
    a = 4;
  // カッコによる優先順位の変化を確認
  } else if (!(true || false) && a == 3) {
    a = 5;
  // 複数の論理否定への対応を確認
  } else if (!(!true || !(a > 4 || false) && !(!(!(b <= 5))))) {
    a = 6;
  }
  return 0;
}
