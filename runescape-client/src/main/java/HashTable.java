import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("hp")
@Implements("HashTable")
public final class HashTable {
   @ObfuscatedName("t")
   @Export("size")
   int size;
   @ObfuscatedName("q")
   @ObfuscatedSignature(
      signature = "[Lhx;"
   )
   @Export("buckets")
   Node[] buckets;
   @ObfuscatedName("i")
   @ObfuscatedSignature(
      signature = "Lhx;"
   )
   @Export("currentGet")
   Node currentGet;
   @ObfuscatedName("a")
   @ObfuscatedSignature(
      signature = "Lhx;"
   )
   @Export("current")
   Node current;
   @ObfuscatedName("l")
   @Export("index")
   int index;

   public HashTable(int var1) {
      this.index = 0;
      this.size = var1;
      this.buckets = new Node[var1];

      for(int var2 = 0; var2 < var1; ++var2) {
         Node var3 = this.buckets[var2] = new Node();
         var3.next = var3;
         var3.previous = var3;
      }

   }

   @ObfuscatedName("t")
   @ObfuscatedSignature(
      signature = "(J)Lhx;"
   )
   @Export("get")
   public Node get(long var1) {
      Node var3 = this.buckets[(int)(var1 & (long)(this.size - 1))];

      for(this.currentGet = var3.next; var3 != this.currentGet; this.currentGet = this.currentGet.next) {
         if(this.currentGet.hash == var1) {
            Node var4 = this.currentGet;
            this.currentGet = this.currentGet.next;
            return var4;
         }
      }

      this.currentGet = null;
      return null;
   }

   @ObfuscatedName("q")
   @ObfuscatedSignature(
      signature = "(Lhx;J)V"
   )
   @Export("put")
   public void put(Node var1, long var2) {
      if(var1.previous != null) {
         var1.unlink();
      }

      Node var4 = this.buckets[(int)(var2 & (long)(this.size - 1))];
      var1.previous = var4.previous;
      var1.next = var4;
      var1.previous.next = var1;
      var1.next.previous = var1;
      var1.hash = var2;
   }

   @ObfuscatedName("i")
   @Export("clear")
   void clear() {
      for(int var1 = 0; var1 < this.size; ++var1) {
         Node var2 = this.buckets[var1];

         while(true) {
            Node var3 = var2.next;
            if(var3 == var2) {
               break;
            }

            var3.unlink();
         }
      }

      this.currentGet = null;
      this.current = null;
   }

   @ObfuscatedName("a")
   @ObfuscatedSignature(
      signature = "()Lhx;"
   )
   @Export("first")
   public Node first() {
      this.index = 0;
      return this.next();
   }

   @ObfuscatedName("l")
   @ObfuscatedSignature(
      signature = "()Lhx;"
   )
   @Export("next")
   public Node next() {
      Node var1;
      if(this.index > 0 && this.buckets[this.index - 1] != this.current) {
         var1 = this.current;
         this.current = var1.next;
         return var1;
      } else {
         do {
            if(this.index >= this.size) {
               return null;
            }

            var1 = this.buckets[this.index++].next;
         } while(var1 == this.buckets[this.index - 1]);

         this.current = var1.next;
         return var1;
      }
   }
}
