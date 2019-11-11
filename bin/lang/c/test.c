int a;
int b();
int c();
int* d();
void e(int,int*);

func int b() {
	int x,a;
	if (true) {
		return a;
	} else {
		return 6;
	}
}

func void e(int g, int *h) {
	output a;
	return;
}

func int c() {
	int c[20], i;
	int *j;
	a = 2;
//	b = a + 3;
//	c[19] = b + 5;
	output c[19];
	call e(i,j);
	a = b() + b();
	return 1 * 3 + a;
}
